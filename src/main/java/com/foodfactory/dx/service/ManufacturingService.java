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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 製造管理(フェーズ2)の中核ロジック。
 *
 * バッチのライフサイクル:
 *   DRAFT --confirmPlan--> PLAN --execute--> MANUFACTURING --complete--> COMPLETED
 *          \--cancel--> CANCELLED           \--reject--> REJECTED
 *
 * 【前提として置いている仮定(要件定義書 8.4節を参照)】
 * recipe_item.use_qty は「商品1個あたりの使用量」ではなく、
 * 「そのバッチを1回実行するために必要な固定量」として扱っている。
 *
 * 【MrpServiceとの循環依存について】
 * MrpServiceはバッチ作成のために本クラス(ManufacturingService)に依存する。
 * 一方、本クラスはCANCELLED/REJECTED発生時にMRPを即座に再計算したいため、
 * MrpServiceを呼び出す必要がある。このままでは「AがBを呼び、BもAを呼ぶ」という
 * 循環依存になりSpringがBean生成に失敗するため、MrpServiceの注入だけ
 * @Lazy(遅延初期化)にすることで、循環を解消している。
 */
@Service
public class ManufacturingService {

    private final ManufacturingBatchMapper manufacturingBatchMapper;
    private final BatchMaterialUsageMapper batchMaterialUsageMapper;
    private final ItemMapper itemMapper;
    private final RecipeItemMapper recipeItemMapper;
    private final MaterialMapper materialMapper;
    private final MaterialLotMapper materialLotMapper;
    private final MrpService mrpService;

    public ManufacturingService(ManufacturingBatchMapper manufacturingBatchMapper,
                                 BatchMaterialUsageMapper batchMaterialUsageMapper,
                                 ItemMapper itemMapper,
                                 RecipeItemMapper recipeItemMapper,
                                 MaterialMapper materialMapper,
                                 MaterialLotMapper materialLotMapper,
                                 @Lazy MrpService mrpService) {
        this.manufacturingBatchMapper = manufacturingBatchMapper;
        this.batchMaterialUsageMapper = batchMaterialUsageMapper;
        this.itemMapper = itemMapper;
        this.recipeItemMapper = recipeItemMapper;
        this.materialMapper = materialMapper;
        this.materialLotMapper = materialLotMapper;
        this.mrpService = mrpService;
    }

    /**
     * 製造バッチを新規作成する(DRAFT、手動追加)。
     * plannedQtyは商品マスタのstandardBatchQtyをそのまま採用する
     * (フェーズ0で「縮小バッチは作らない」と決めたため、常に標準量で固定)。
     */
    @Transactional
    public ManufacturingBatch createBatch(Long itemId, LocalDate batchDate, String createdBy) {
        return buildAndInsertBatch(itemId, batchDate, createdBy,
                ManufacturingBatch.OriginType.MANUAL, null);
    }

    /**
     * MRPが自動生成するバッチを作成する(DRAFT、MRP_AUTO)。
     * MrpServiceから呼ばれる想定。createBatchとほぼ同じだが、
     * originType/mrpRunIdが異なる点だけを区別するため、共通のbuildAndInsertBatchに集約している。
     */
    @Transactional
    public ManufacturingBatch createAutoBatch(Long itemId, LocalDate batchDate, Long mrpRunId) {
        return buildAndInsertBatch(itemId, batchDate, null,
                ManufacturingBatch.OriginType.MRP_AUTO, mrpRunId);
    }

    private ManufacturingBatch buildAndInsertBatch(Long itemId, LocalDate batchDate, String createdBy,
                                                     ManufacturingBatch.OriginType originType, Long mrpRunId) {
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
        batch.setOriginType(originType);
        batch.setMrpRunId(mrpRunId);
        // statusはフィールドの初期値(DRAFT)のまま使う。

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
     * 複数のバッチをまとめてDRAFT → PLANに確定する(一括確定)。
     * 画面上は「Draftを1件ずつPlanエリアに移動させる」操作を人が行い、
     * 最後に「選ばれたものをまとめて確定する」ボタンを押す、という使い方を想定している。
     * 1件でも対象外の状態(DRAFT以外)が混ざっていた場合、その時点で例外を投げて処理を止める
     * (一部だけ確定して残りは失敗、という中途半端な状態を避けるため、
     *  このメソッド全体は呼び出し側で@TransactionalなconfirmPlanを都度呼ぶ形にしている)。
     */
    @Transactional
    public void confirmPlanBulk(List<Long> batchIds) {
        for (Long batchId : batchIds) {
            confirmPlan(batchId);
        }
    }

    /**
     * 一定期間(days日)操作されずDRAFTのまま放置されているバッチを取得する。
     * MRPが自動生成したDraftが、確定も取り消しもされないまま埋もれてしまい、
     * 運用者が対応漏れに気づけなくなることを防ぐための一覧取得用メソッド。
     */
    public List<ManufacturingBatch> listStaleDrafts(int days) {
        return manufacturingBatchMapper.findStaleDrafts(days);
    }

    /**
     * 製造開始前のバッチを取り消す(DRAFT/PLAN → CANCELLED)。
     *
     * 取り消した瞬間、そのバッチが供給予定量から消えることになるため、
     * MRPを即座に再計算(EVENTトリガー)し、本当は不足していないかをその場で確認する。
     * これにより「キャンセルしたのに、次のMRP定期実行まで不足に気づかない」という
     * タイムラグを実質的になくしている。
     */
    @Transactional
    public void cancelBatch(Long batchId, String cancelComment) {
        ManufacturingBatch batch = getBatchOrThrow(batchId);
        if (batch.getStatus() != ManufacturingBatch.Status.DRAFT
                && batch.getStatus() != ManufacturingBatch.Status.PLAN) {
            throw new IllegalStateException(
                    "DRAFTまたはPLAN状態のバッチのみ取り消せます。現在の状態: " + batch.getStatus());
        }
        manufacturingBatchMapper.cancelBatch(batchId, cancelComment);
        mrpService.runForItem(batch.getItemId(), com.foodfactory.dx.domain.MrpRun.TriggeredBy.EVENT);
    }

    /**
     * 指定した商品のレシピをもとに、FEFO(期限が近い順)で材料を自動選定する。
     * このメソッド自体はDBを変更しない(在庫を減らしたりはしない)、あくまで「計算結果のプレビュー」。
     */
    public FefoAllocationResult previewFefoAllocation(Long itemId) {
        List<RecipeItem> recipeItems = recipeItemMapper.findByItemId(itemId);
        FefoAllocationResult result = new FefoAllocationResult();

        for (RecipeItem recipeItem : recipeItems) {
            BigDecimal need = recipeItem.getUseQty();

            // 材料が原料(RAW)か添加物(ADDITIVE)かで、FEFO選定の基準を切り替える。
            //   原料  : 産地 + 賞味期限のルールで選定する(allowedOriginsで絞り込む)
            //   添加物: 賞味期限のみで選定する(産地の概念がそもそも無いため)
            // 【修正した不具合】以前は材料種別を区別せず一律で産地フィルターをかけていたため、
            // 添加物のallowedOrigins(常に空)によって「空リストに含まれるものは無い」という判定になり、
            // 添加物はどのロットも絶対に選定できず、FEFO計算が必ず失敗する不具合があった。
            Material material = materialMapper.findById(recipeItem.getMaterialId()).orElse(null);
            boolean isRawMaterial = material != null && material.getCategory() == Material.Category.RAW;
            List<String> allowedOrigins = isRawMaterial ? recipeItem.getAllowedOriginList() : null;

            List<MaterialLot> candidateLots = materialLotMapper
                    .findByMaterialIdOrderByExpiry(recipeItem.getMaterialId());

            BigDecimal remainingNeed = need;
            for (MaterialLot lot : candidateLots) {
                if (remainingNeed.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
                // 原料の場合のみ産地チェックを行う。添加物(allowedOrigins == null)は無条件で候補にする。
                if (allowedOrigins != null && !allowedOrigins.contains(lot.getOrigin())) {
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
                String materialName = (material != null) ? material.getName() : "ID=" + recipeItem.getMaterialId();
                result.getShortageMaterialNames().add(materialName);
            }
        }

        return result;
    }

    /** 製造を実行する(PLAN → MANUFACTURING)。 */
    @Transactional
    public void executeBatch(Long batchId, List<ActualUsageInput> actualUsages) {
        ManufacturingBatch batch = getBatchOrThrow(batchId);
        if (batch.getStatus() != ManufacturingBatch.Status.PLAN) {
            throw new IllegalStateException(
                    "PLAN状態のバッチのみ実行できます。現在の状態: " + batch.getStatus());
        }

        FefoAllocationResult preview = previewFefoAllocation(batch.getItemId());
        Map<Long, BigDecimal> suggestedByLotId = new HashMap<>();
        for (FefoAllocationLine line : preview.getLines()) {
            suggestedByLotId.put(line.getMaterialLotId(), line.getAllocatedQty());
        }

        for (ActualUsageInput input : actualUsages) {
            MaterialLot lot = materialLotMapper.findById(input.getMaterialLotId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "指定された材料ロットが見つかりません: lotId=" + input.getMaterialLotId()));

            if (lot.getRemainingQty().compareTo(input.getUsedQty()) < 0) {
                throw new IllegalArgumentException(
                        "ロットの残量が不足しています。lotId=" + lot.getLotId()
                                + ", 残量=" + lot.getRemainingQty()
                                + ", 使用予定量=" + input.getUsedQty());
            }

            int updatedRows = materialLotMapper.decrementRemainingQty(lot.getLotId(), input.getUsedQty());
            if (updatedRows == 0) {
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

    /** 検品完了処理(MANUFACTURING → COMPLETED)。 */
    @Transactional
    public void completeBatch(Long batchId, BigDecimal acceptedQty, BigDecimal lossQty, String lossComment) {
        ManufacturingBatch batch = getBatchOrThrow(batchId);
        if (batch.getStatus() != ManufacturingBatch.Status.MANUFACTURING) {
            throw new IllegalStateException(
                    "MANUFACTURING状態のバッチのみ完了できます。現在の状態: " + batch.getStatus());
        }
        BigDecimal producedQty = acceptedQty.add(lossQty);
        boolean exceededPlan = producedQty.compareTo(batch.getPlannedQty()) > 0;
        manufacturingBatchMapper.completeBatch(
                batchId, producedQty, acceptedQty, lossQty, lossComment, exceededPlan);
    }

    /**
     * バッチ全体を破棄する(MANUFACTURING → REJECTED)。重大な異常が見つかった場合のみ使う。
     *
     * REJECTEDになったバッチも、CANCELLEDと同様に供給予定から消えることになるため、
     * 同じくMRPを即座に再計算する(EVENTトリガー)。
     */
    @Transactional
    public void rejectBatch(Long batchId, String rejectComment) {
        ManufacturingBatch batch = getBatchOrThrow(batchId);
        if (batch.getStatus() != ManufacturingBatch.Status.MANUFACTURING) {
            throw new IllegalStateException(
                    "MANUFACTURING状態のバッチのみ破棄できます。現在の状態: " + batch.getStatus());
        }
        manufacturingBatchMapper.rejectBatch(batchId, rejectComment);
        mrpService.runForItem(batch.getItemId(), com.foodfactory.dx.domain.MrpRun.TriggeredBy.EVENT);
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
