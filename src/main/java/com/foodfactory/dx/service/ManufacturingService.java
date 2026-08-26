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
import java.util.ArrayList;
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

        // batchDateがnull(=まだどの日にも配置されていない、未配置プールのDraft)の場合、
        // 「その日の何バッチ目か」という概念自体がまだ存在しないため、batchSeqも採番せずnullのままにする。
        // 後日、デイリー画面で特定の日に配置された時点で、batchDateとbatchSeqが初めて確定する
        // (ManufacturingBatchService.assignToDateのようなメソッドで採番する想定)。
        Integer nextSeq = null;
        if (batchDate != null) {
            Integer maxSeq = manufacturingBatchMapper.findMaxBatchSeq(itemId, batchDate);
            nextSeq = (maxSeq == null) ? 1 : maxSeq + 1;
        }

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

    /**
     * 未配置プールのDraftを、特定の日付に配置する(デイリー画面で、
     * バッジをタップ/ドラッグして「〇月〇日の予定」に移す操作に対応)。
     * その日・その商品の何バッチ目かを、この時点で初めて採番する。
     */
    @Transactional
    public void assignToDate(Long batchId, LocalDate batchDate) {
        ManufacturingBatch batch = getBatchOrThrow(batchId);
        if (batch.getStatus() != ManufacturingBatch.Status.DRAFT) {
            throw new IllegalStateException(
                    "DRAFT状態のバッチのみ配置できます。現在の状態: " + batch.getStatus());
        }
        if (batch.getBatchDate() != null) {
            throw new IllegalStateException("このバッチは既に配置済みです(batchDate=" + batch.getBatchDate() + ")");
        }

        Integer maxSeq = manufacturingBatchMapper.findMaxBatchSeq(batch.getItemId(), batchDate);
        int nextSeq = (maxSeq == null) ? 1 : maxSeq + 1;

        int updated = manufacturingBatchMapper.assignToDate(batchId, batchDate, nextSeq);
        if (updated == 0) {
            throw new IllegalStateException("配置に失敗しました(既に他の操作で配置済みの可能性があります)。batchId=" + batchId);
        }
    }

    /**
     * 特定の日付に配置したDraftを、未配置プールに戻す(誤って配置した場合の取り消し)。
     * PLAN確定済みのものは対象外(その場合はcancelBatchで取り消す)。
     */
    @Transactional
    public void unassignFromDate(Long batchId) {
        ManufacturingBatch batch = getBatchOrThrow(batchId);
        if (batch.getStatus() != ManufacturingBatch.Status.DRAFT) {
            throw new IllegalStateException(
                    "DRAFT状態のバッチのみ、未配置に戻せます。現在の状態: " + batch.getStatus());
        }
        manufacturingBatchMapper.unassignFromDate(batchId);
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
            List<FefoAllocationLine> lines = allocateForMaterial(recipeItem.getMaterialId(), recipeItem.getUseQty(),
                    recipeItem.getAllowedOriginList());
            result.getLines().addAll(lines);

            BigDecimal allocated = lines.stream().map(FefoAllocationLine::getAllocatedQty)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (allocated.compareTo(recipeItem.getUseQty()) < 0) {
                result.setShortage(true);
                Material material = materialMapper.findById(recipeItem.getMaterialId()).orElse(null);
                String materialName = (material != null) ? material.getName() : "ID=" + recipeItem.getMaterialId();
                result.getShortageMaterialNames().add(materialName);
            }
        }

        return result;
    }

    /**
     * 1つの材料について、必要量(need)を満たすまで、FEFO順(賞味期限が近い順)に
     * ロットを引き当てる。needs_review=trueのロットは、findByMaterialIdOrderByExpiry
     * (Mapper層)の時点で既に除外されている。
     *
     * previewFefoAllocation(全材料分の一括プレビュー)と、「別ロットに切り替える」操作
     * (1材料分だけを再選定する)の、両方から共通して使う。
     */
    private List<FefoAllocationLine> allocateForMaterial(Long materialId, BigDecimal need, List<String> allowedOriginsIfRaw) {
        Material material = materialMapper.findById(materialId).orElse(null);
        boolean isRawMaterial = material != null && material.getCategory() == Material.Category.RAW;
        // 材料が原料(RAW)か添加物(ADDITIVE)かで、FEFO選定の基準を切り替える。
        //   原料  : 産地 + 賞味期限のルールで選定する(allowedOriginsで絞り込む)
        //   添加物: 賞味期限のみで選定する(産地の概念がそもそも無いため)
        List<String> allowedOrigins = isRawMaterial ? allowedOriginsIfRaw : null;

        List<MaterialLot> candidateLots = materialLotMapper.findByMaterialIdOrderByExpiry(materialId);

        List<FefoAllocationLine> lines = new ArrayList<>();
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
            lines.add(new FefoAllocationLine(materialId, lot.getLotId(), lot.getSupplierLotNo(),
                    lot.getOrigin(), allocate, lot.getOriginHoldId()));
            remainingNeed = remainingNeed.subtract(allocate);
        }
        return lines;
    }

    /**
     * 製造実行画面で「別ロットに切り替える」操作を行った際の処理。
     *
     * 1. 元のロット(lotId)に要確認フラグを立てる(残量自体は変更しない。
     *    人が検査結果を登録するまで、判断を保留する設計。8.21節を参照)。
     * 2. その材料について、あらためてFEFO選定を行う(要確認フラグにより、
     *    元のロットは候補から自動的に除外される)。全量を、新しいロット(群)から
     *    改めて選び直す(以前の実測値は、呼び出し元(フロント)で破棄する前提)。
     *
     * @return 新しく選定されたロットの一覧(1件とは限らない。新しいロットでも
     *         不足する場合、複数ロットにまたがることがある)
     */
    @Transactional
    public List<FefoAllocationLine> switchLot(Long lotId, MaterialLot.ReviewReason reviewReason,
                                               String reviewComment, Long itemId) {
        MaterialLot targetLot = materialLotMapper.findById(lotId)
                .orElseThrow(() -> new IllegalArgumentException("指定された材料ロットが見つかりません: lotId=" + lotId));

        materialLotMapper.markNeedsReview(lotId, reviewReason, reviewComment);

        // 元のロットが、レシピの中でどれだけの量を担っていたか(=必要量)を、
        // レシピから再取得して、その全量を新しいロットで賄えるよう再計算する。
        RecipeItem recipeItem = recipeItemMapper.findByItemId(itemId).stream()
                .filter(ri -> ri.getMaterialId().equals(targetLot.getMaterialId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "指定された商品のレシピに、この材料が見つかりません: itemId=" + itemId + ", materialId=" + targetLot.getMaterialId()));

        return allocateForMaterial(targetLot.getMaterialId(), recipeItem.getUseQty(), recipeItem.getAllowedOriginList());
    }

    /**
     * ロットを要確認状態にする(needs_reviewを立てるだけの単独操作)。
     * 在庫画面(棚卸・日常の在庫管理)から呼ばれる想定。製造中のバッチとは無関係に、
     * 「このロットを、以降の材料の自動選定から外したい」という単独の判断で使う。
     *
     * 【製造実行画面のswitchLotとの違い】switchLotは、要確認にすると同時に
     * 「今使おうとしていた分を、別のロットで賄い直す」という再選定もセットで行うが、
     * こちらは要確認にするだけで、再選定は行わない(在庫画面には、代替ロットを
     * その場に表示する文脈が無いため)。
     */
    @Transactional
    public void markLotAsNeedsReview(Long lotId, MaterialLot.ReviewReason reviewReason, String reviewComment) {
        materialLotMapper.findById(lotId)
                .orElseThrow(() -> new IllegalArgumentException("指定された材料ロットが見つかりません: lotId=" + lotId));
        materialLotMapper.markNeedsReview(lotId, reviewReason, reviewComment);
    }

    /** 製造を実行する(PLAN → MANUFACTURING)。 */
    @Transactional
    public void executeBatch(Long batchId, List<ActualUsageInput> actualUsages, BigDecimal actualHydrationQty) {
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

        // 水はそもそも材料マスタ・在庫の対象外(在庫を消費するものではない)ため、
        // batch_material_usageではなく、manufacturing_batchの専用列に直接記録する
        // (トレーサビリティ記録用。「加水合計」画面表示は、この値と液体添加物の
        // 実測値合計を、フロント側で合算して求める)。
        manufacturingBatchMapper.updateActualHydrationQty(batchId, actualHydrationQty);

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

    /**
     * 指定したバッチが、「結局受け入れ」経由のロットを使用したかを判定する。
     * 実行済み一覧・デイリー画面などで、保留対応を経た材料であることを示す
     * バッジ表示に使う(要件定義書8.23節を参照)。
     */
    public boolean usedHeldLot(Long batchId) {
        return batchMaterialUsageMapper.usedHeldLot(batchId);
    }

    /**
     * 指定したバッチが、一度でも「要確認」→検査結果登録を経たロットを使用したかを判定する。
     * usedHeldLotとはリスクの原因が異なるため、別々のバッジとして表示する想定。
     */
    public boolean usedReviewedLot(Long batchId) {
        return batchMaterialUsageMapper.usedReviewedLot(batchId);
    }

    private ManufacturingBatch getBatchOrThrow(Long batchId) {
        return manufacturingBatchMapper.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("指定されたバッチが見つかりません: batchId=" + batchId));
    }
}
