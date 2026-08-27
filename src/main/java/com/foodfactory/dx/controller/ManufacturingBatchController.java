package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.BatchMaterialUsage;
import com.foodfactory.dx.domain.ManufacturingBatch;
import com.foodfactory.dx.dto.ActualUsageInput;
import com.foodfactory.dx.dto.AssignToDateRequest;
import com.foodfactory.dx.dto.BatchMaterialUsageDetail;
import com.foodfactory.dx.dto.CancelBatchRequest;
import com.foodfactory.dx.dto.CompleteBatchRequest;
import com.foodfactory.dx.dto.ConfirmPlanBulkRequest;
import com.foodfactory.dx.dto.CreateBatchRequest;
import com.foodfactory.dx.dto.DiscardItemStockRequest;
import com.foodfactory.dx.dto.ExecuteBatchRequest;
import com.foodfactory.dx.dto.FefoAllocationResult;
import com.foodfactory.dx.dto.RejectBatchRequest;
import com.foodfactory.dx.service.ItemStockService;
import com.foodfactory.dx.service.ManufacturingService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ManufacturingBatchController {

    private final ManufacturingService manufacturingService;
    private final ItemStockService itemStockService;

    public ManufacturingBatchController(ManufacturingService manufacturingService, ItemStockService itemStockService) {
        this.manufacturingService = manufacturingService;
        this.itemStockService = itemStockService;
    }

    /** 指定した商品のバッチ(Draft)を新規作成する。 */
    @PostMapping("/api/items/{itemId}/batches")
    public ResponseEntity<ManufacturingBatch> createBatch(
            @PathVariable Long itemId,
            @RequestBody CreateBatchRequest request) {
        ManufacturingBatch batch = manufacturingService.createBatch(
                itemId, request.getBatchDate(), request.getCreatedBy());
        return ResponseEntity.status(HttpStatus.CREATED).body(batch);
    }

    /** 指定した商品のレシピについて、FEFO自動選定の結果をプレビューする(在庫は変更しない)。 */
    @GetMapping("/api/items/{itemId}/fefo-preview")
    public FefoAllocationResult previewFefo(@PathVariable Long itemId) {
        return manufacturingService.previewFefoAllocation(itemId);
    }

    @GetMapping("/api/batches")
    public List<ManufacturingBatch> list() {
        return manufacturingService.listAll();
    }

    @GetMapping("/api/batches/{batchId}/usages")
    public List<BatchMaterialUsage> listUsages(@PathVariable Long batchId) {
        return manufacturingService.listUsagesByBatchId(batchId);
    }

    /** 製造実績一覧の「使用材料の内訳を見る」用に、材料名・ロット番号を含めた詳細一覧を返す。 */
    @GetMapping("/api/batches/{batchId}/usage-details")
    public List<BatchMaterialUsageDetail> listUsageDetails(@PathVariable Long batchId) {
        return manufacturingService.listUsageDetailsByBatchId(batchId);
    }

    /** そのバッチが「結局受け入れ」経由のロットを使用したかを判定する(バッジ表示用)。 */
    @GetMapping("/api/batches/{batchId}/used-held-lot")
    public boolean usedHeldLot(@PathVariable Long batchId) {
        return manufacturingService.usedHeldLot(batchId);
    }

    /** そのバッチが「要確認→検査結果登録」を経たロットを使用したかを判定する(バッジ表示用)。 */
    @GetMapping("/api/batches/{batchId}/used-reviewed-lot")
    public boolean usedReviewedLot(@PathVariable Long batchId) {
        return manufacturingService.usedReviewedLot(batchId);
    }

    /**
     * 商品在庫タブ(バックヤード担当)から、COMPLETED状態の商品ロットを廃棄する。
     * MANUFACTURING中の破棄(reject)とは別の、在庫保管中に見つかった不良への対応
     * (要件定義書8.25節を参照)。
     */
    @PostMapping("/api/batches/{batchId}/discard-item-stock")
    public ResponseEntity<Void> discardItemStock(@PathVariable Long batchId, @RequestBody DiscardItemStockRequest request) {
        itemStockService.discardItemStock(batchId, request.getDiscardQty(), request.getReason(), request.getComment());
        return ResponseEntity.ok().build();
    }

    /** DRAFT → PLAN。バッチの内容を確定する。 */
    @PostMapping("/api/batches/{batchId}/confirm-plan")
    public ResponseEntity<Void> confirmPlan(@PathVariable Long batchId) {
        manufacturingService.confirmPlan(batchId);
        return ResponseEntity.ok().build();
    }

    /**
     * 未配置プールのDraftを、特定の日付に配置する
     * (デイリー画面で、バッジをタップ/ドラッグして「〇月〇日の予定」に移す操作に対応)。
     */
    @PostMapping("/api/batches/{batchId}/assign-date")
    public ResponseEntity<Void> assignToDate(@PathVariable Long batchId, @RequestBody AssignToDateRequest request) {
        manufacturingService.assignToDate(batchId, request.getBatchDate());
        return ResponseEntity.ok().build();
    }

    /** 配置済みのDraftを、未配置プールに戻す(誤って配置した場合の取り消し)。 */
    @PostMapping("/api/batches/{batchId}/unassign-date")
    public ResponseEntity<Void> unassignFromDate(@PathVariable Long batchId) {
        manufacturingService.unassignFromDate(batchId);
        return ResponseEntity.ok().build();
    }

    /**
     * 複数のバッチをまとめてDRAFT → PLANに確定する(一括確定)。
     * 画面上で複数のDraftカードを選んでから、まとめて確定する操作に対応する想定。
     */
    @PostMapping("/api/batches/confirm-plan-bulk")
    public ResponseEntity<Void> confirmPlanBulk(@RequestBody ConfirmPlanBulkRequest request) {
        manufacturingService.confirmPlanBulk(request.getBatchIds());
        return ResponseEntity.ok().build();
    }

    /**
     * status=DRAFTのまま、指定した日数以上放置されているバッチの一覧を取得する。
     * 例: GET /api/batches/stale-drafts?days=3
     */
    @GetMapping("/api/batches/stale-drafts")
    public List<ManufacturingBatch> listStaleDrafts(@RequestParam(defaultValue = "3") int days) {
        return manufacturingService.listStaleDrafts(days);
    }

    /** DRAFT/PLAN → CANCELLED。製造開始前にバッチを取り消す(MRPが即座に再計算される)。 */
    @PostMapping("/api/batches/{batchId}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long batchId,
            @RequestBody CancelBatchRequest request) {
        manufacturingService.cancelBatch(batchId, request.getCancelComment());
        return ResponseEntity.ok().build();
    }

    /** PLAN → MANUFACTURING。作業員が実測した使用量(材料+加水)をもとに製造を実行する。 */
    @PostMapping("/api/batches/{batchId}/execute")
    public ResponseEntity<Void> execute(
            @PathVariable Long batchId,
            @RequestBody ExecuteBatchRequest request) {
        manufacturingService.executeBatch(batchId, request.getActualUsages(), request.getActualHydrationQty());
        return ResponseEntity.ok().build();
    }

    /** MANUFACTURING → COMPLETED。検品結果(合格数・軽微な不良数)を確定する。 */
    @PostMapping("/api/batches/{batchId}/complete")
    public ResponseEntity<Void> complete(
            @PathVariable Long batchId,
            @RequestBody CompleteBatchRequest request) {
        manufacturingService.completeBatch(
                batchId, request.getAcceptedQty(), request.getLossQty(), request.getLossComment());
        return ResponseEntity.ok().build();
    }

    /** MANUFACTURING → REJECTED。重大な異常によりバッチ全体を破棄する。 */
    @PostMapping("/api/batches/{batchId}/reject")
    public ResponseEntity<Void> reject(
            @PathVariable Long batchId,
            @RequestBody RejectBatchRequest request) {
        manufacturingService.rejectBatch(batchId, request.getRejectComment());
        return ResponseEntity.ok().build();
    }
}
