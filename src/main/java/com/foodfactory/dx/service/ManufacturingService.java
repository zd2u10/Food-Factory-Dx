package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.BatchMaterialUsage;
import com.foodfactory.dx.domain.Item;
import com.foodfactory.dx.domain.ManufacturingBatch;
import com.foodfactory.dx.domain.Material;
import com.foodfactory.dx.domain.MaterialLot;
import com.foodfactory.dx.domain.RecipeItem;
import com.foodfactory.dx.dto.ActualUsageInput;
import com.foodfactory.dx.dto.FefoAllocationLine;
import com.foodfactory.dx.dto.FefoAllocationResult;
import com.foodfactory.dx.mapper.BatchMaterialUsageMapper;
import com.foodfactory.dx.mapper.ItemMapper;
import com.foodfactory.dx.mapper.ManufacturingBatchMapper;
import com.foodfactory.dx.mapper.MaterialLotMapper;
import com.foodfactory.dx.mapper.MaterialMapper;
import com.foodfactory.dx.mapper.RecipeItemMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 製造管理(フェーズ2)の中核ロジック。
 *
 * バッチのライフサイクル:
 *   DRAFT --confirmPlan--> PLAN --execute--> MANUFACTURING --complete--> COMPLETED
 *                                                          \--reject--> REJECTED
 *
 * 【前提として置いている仮定(要件定義書 8.3節と合わせて確認したい点)】
 * recipe_item.use_qty は「商品1個あたりの使用量」ではなく、
 * 「そのバッチを1回実行するために必要な固定量」として扱っている
 * (例: 主原料15kgを1バッチにつき1袋使う、という決定に基づく)。
 * そのため、バッチの計画数量(plannedQty)を変えても、FEFOで引き当てる必要量(need)は
 * 変化させず、常にrecipe_item.use_qtyをそのまま使う。
 */
@Service
public class ManufacturingService {

    private final ManufacturingBatchMapper manufacturingBatchMapper;
    private final BatchMaterialUsageMapper batchMaterialUsageMapper;
    private final ItemMapper itemMapper;
    private final RecipeItemMapper recipeItemMapper;
    private final MaterialMapper materialMapper;
    private final MaterialLotMapper materialLotMapper;

    public ManufacturingService(ManufacturingBatchMapper manufacturingBatchMapper,
                                 BatchMaterialUsageMapper batchMaterialUsageMapper,
                                 ItemMapper itemMapper,
                                 RecipeItemMapper recipeItemMapper,
                                 MaterialMapper materialMapper,
                                 MaterialLotMapper materialLotMapper) {
        this.manufacturingBatchMapper = manufacturingBatchMapper;
        this.batchMaterialUsageMapper = batchMaterialUsageMapper;
        this.itemMapper = itemMapper;
        this.recipeItemMapper = recipeItemMapper;
        this.materialMapper = materialMapper;
        this.materialLotMapper = materialLotMapper;
    }

    /**
     * 製造バッチを新規作成する(DRAFTまたはPLAN、手動追加)。
     * plannedQtyは商品マスタのstandardBatchQtyをそのまま採用する
     * (フェーズ0で「縮小バッチは作らない」と決めたため、常に標準量で固定)。
     */
    @Transactional
    public ManufacturingBatch createBatch(Long itemId, LocalDate batchDate, String createdBy) {
        Item item = itemMapper.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("指定された商品が見つかりません: itemId=" + itemId));

        Integer maxSeq = manufacturingBatchMapper.findMaxBatchSeq(itemId, batchDate);
        int nextSeq = (maxSeq == null) ? 1 : maxSeq + 1;

        ManufacturingBatch batch = new ManufacturingBatch();
        batch.setItemId(itemId);
        batch.setBatchDate(batchDate);
        batch.setBatchSeq(nextSeq);
        batch.setCreatedBy(createdBy);
        batch.setPlannedQty(item.getStandardBatchQty());
        // status/originTypeはフィールドの初期値(DRAFT/MANUAL)のまま使う。
        // フェーズ2時点ではMRP自動化(フェーズ4)が無いため、originTypeは常にMANUALになる。

        manufacturingBatchMapper.insert(batch);
        return batch;
    }

    /** DRAFT → PLAN への遷移。人が内容を確認し、確定させる操作に対応する。 */
    @Transactional
    public void confirmPlan(Long batchId) {
        ManufacturingBatch batch = getBatchOrThrow(batchId);
        if (batch.getStatus() != ManufacturingBatch.Status.DRAFT) {
            throw new IllegalStateException(
                    "DRAFT状態のバッチのみ確定できます。現在の状態: " + batch.getStatus());
        }
        manufacturingBatchMapper.updateStatus(batchId, ManufacturingBatch.Status.PLAN);
    }

    /**
     * 指定した商品のレシピをもとに、FEFO(期限が近い順)で材料を自動選定する。
     * このメソッド自体はDBを変更しない(在庫を減らしたりはしない)、あくまで「計算結果のプレビュー」。
     *
     * 産地制約(allowedOrigins)を満たすロットだけで必要量に届かない場合、
     * shortage=true として返す。フェーズ0で決めた通り、この場合は現場判断での代替を許可せず、
     * 呼び出し側(Controller)で製造実行そのものをブロックする想定。
     */
    public FefoAllocationResult previewFefoAllocation(Long itemId) {
        List<RecipeItem> recipeItems = recipeItemMapper.findByItemId(itemId);
        FefoAllocationResult result = new FefoAllocationResult();

        for (RecipeItem recipeItem : recipeItems) {
            BigDecimal need = recipeItem.getUseQty();
            List<String> allowedOrigins = recipeItem.getAllowedOriginList();

            // findByMaterialIdOrderByExpiryは既にremaining_qty > 0のロットだけを
            // 賞味期限が近い順に返してくれる(フェーズ1のMaterialLotMapperで実装済み)。
            List<MaterialLot> candidateLots = materialLotMapper
                    .findByMaterialIdOrderByExpiry(recipeItem.getMaterialId());

            BigDecimal remainingNeed = need;
            for (MaterialLot lot : candidateLots) {
                if (remainingNeed.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
                // 許可産地に含まれないロットは、そもそも選定候補から除外する
                // (産地制約は現場判断での代替を許可しないため、フィルターの外側で拾わない)。
                if (!allowedOrigins.contains(lot.getOrigin())) {
                    continue;
                }

                BigDecimal allocate = lot.getRemainingQty().min(remainingNeed);
                result.getLines().add(new FefoAllocationLine(
                        recipeItem.getMaterialId(), lot.getLotId(), lot.getSupplierLotNo(),
                        lot.getOrigin(), allocate));
                remainingNeed = remainingNeed.subtract(allocate);
            }

            if (remainingNeed.compareTo(BigDecimal.ZERO) > 0) {
                result.setShortage(true);
                Material material = materialMapper.findById(recipeItem.getMaterialId()).orElse(null);
                String materialName = (material != null) ? material.getName() : "ID=" + recipeItem.getMaterialId();
                result.getShortageMaterialNames().add(materialName);
            }
        }

        return result;
    }

    /**
     * 製造を実行する(PLAN → MANUFACTURING)。
     * 作業員が実際に入力した使用量(actualUsages)をもとに、各材料ロットの残量を減らし、
     * バッチ材料使用記録を残す。
     *
     * @Transactional: 複数ロットの残量更新+使用記録の登録+ステータス更新を、
     *   一つのまとまりとして扱う。途中で1つでも失敗したら全て取り消す。
     */
    @Transactional
    public void executeBatch(Long batchId, List<ActualUsageInput> actualUsages) {
        ManufacturingBatch batch = getBatchOrThrow(batchId);
        if (batch.getStatus() != ManufacturingBatch.Status.PLAN) {
            throw new IllegalStateException(
                    "PLAN状態のバッチのみ実行できます。現在の状態: " + batch.getStatus());
        }

        // 記録用に理論値(FEFOの計算結果)も取得しておく。
        // 実測値と理論値の両方を保存することで、後から計量誤差の傾向を分析できるようにする
        // (要件定義書 3.4節「レシピ(加水率の管理)」および製造実行フローの方針に対応)。
        FefoAllocationResult preview = previewFefoAllocation(batch.getItemId());
        Map<Long, BigDecimal> suggestedByLotId = new HashMap<>();
        for (FefoAllocationLine line : preview.getLines()) {
            suggestedByLotId.put(line.getMaterialLotId(), line.getAllocatedQty());
        }

        for (ActualUsageInput input : actualUsages) {
            MaterialLot lot = materialLotMapper.findById(input.getMaterialLotId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "指定された材料ロットが見つかりません: lotId=" + input.getMaterialLotId()));

            // ここでの残量チェックは「入力の時点で明らかにおかしい値」を早期に弾くための
            // 補助的なチェックであり、本当の安全装置は次の decrementRemainingQty 呼び出しの
            // WHERE句(DB側での条件付き更新)にある。
            // このチェックだけに頼ると、チェックした直後に別の処理が同じロットを消費してしまう
            // 「読み取ってから書き込むまでの間に割り込まれる」競合が起こり得るため、
            // 実際の在庫の増減は必ずDB側の条件判定つきSQLで行う。
            if (lot.getRemainingQty().compareTo(input.getUsedQty()) < 0) {
                throw new IllegalArgumentException(
                        "ロットの残量が不足しています。lotId=" + lot.getLotId()
                                + ", 残量=" + lot.getRemainingQty()
                                + ", 使用予定量=" + input.getUsedQty());
            }

            int updatedRows = materialLotMapper.decrementRemainingQty(lot.getLotId(), input.getUsedQty());
            if (updatedRows == 0) {
                // このタイミングで0件ということは、上のチェックと実際の更新の間に
                // 別の処理が同じロットを消費し、在庫が足りなくなったことを意味する。
                throw new IllegalArgumentException(
                        "ロットの在庫を確保できませんでした(他の処理と競合した可能性があります): lotId="
                                + lot.getLotId());
            }

            BigDecimal suggestedQty = suggestedByLotId.getOrDefault(lot.getLotId(), BigDecimal.ZERO);
            BatchMaterialUsage usage = new BatchMaterialUsage(
                    batchId, lot.getLotId(), suggestedQty, input.getUsedQty(),
                    BatchMaterialUsage.UsageType.CONSUMPTION, null);
            batchMaterialUsageMapper.insert(usage);
        }

        manufacturingBatchMapper.updateStatus(batchId, ManufacturingBatch.Status.MANUFACTURING);
    }

    /**
     * 検品完了処理(MANUFACTURING → COMPLETED)。
     * 通常運用の軽微な不良(数個レベル)はここでlossQtyとして記録し、バッチ自体は完了扱いにする。
     *
     * 【フェーズ2時点で未実装の部分】完了した合格数量(acceptedQty)を、
     * 商品(items)側の在庫として計上する処理はまだ無い(要件定義書8.3節の通り、
     * 商品在庫の追跡はフェーズ5で設計する想定のため)。
     */
    @Transactional
    public void completeBatch(Long batchId, BigDecimal acceptedQty, BigDecimal lossQty, String lossComment) {
        ManufacturingBatch batch = getBatchOrThrow(batchId);
        if (batch.getStatus() != ManufacturingBatch.Status.MANUFACTURING) {
            throw new IllegalStateException(
                    "MANUFACTURING状態のバッチのみ完了できます。現在の状態: " + batch.getStatus());
        }
        BigDecimal producedQty = acceptedQty.add(lossQty);
        manufacturingBatchMapper.completeBatch(batchId, producedQty, acceptedQty, lossQty, lossComment);
    }

    /**
     * バッチ全体を破棄する(MANUFACTURING → REJECTED)。重大な異常が見つかった場合のみ使う。
     *
     * 注意: 既に消費した材料(batch_material_usage)は取り消さない。
     * 材料は物理的に既に使われてしまっているため、バッチの結果が不良品扱いになったとしても
     * 材料の消費という事実そのものは変わらない、という考え方による
     * (要件定義書 3.2節「廃棄・ロスの扱い」を参照)。
     */
    @Transactional
    public void rejectBatch(Long batchId, String rejectComment) {
        ManufacturingBatch batch = getBatchOrThrow(batchId);
        if (batch.getStatus() != ManufacturingBatch.Status.MANUFACTURING) {
            throw new IllegalStateException(
                    "MANUFACTURING状態のバッチのみ破棄できます。現在の状態: " + batch.getStatus());
        }
        manufacturingBatchMapper.rejectBatch(batchId, rejectComment);
    }

    public List<ManufacturingBatch> listAll() {
        return manufacturingBatchMapper.findAll();
    }

    public List<BatchMaterialUsage> listUsagesByBatchId(Long batchId) {
        return batchMaterialUsageMapper.findByBatchId(batchId);
    }

    private ManufacturingBatch getBatchOrThrow(Long batchId) {
        return manufacturingBatchMapper.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("指定されたバッチが見つかりません: batchId=" + batchId));
    }
}
