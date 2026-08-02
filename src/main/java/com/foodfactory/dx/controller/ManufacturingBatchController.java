package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.BatchMaterialUsage;
import com.foodfactory.dx.domain.ManufacturingBatch;
import com.foodfactory.dx.dto.ActualUsageInput;
import com.foodfactory.dx.dto.CancelBatchRequest;
import com.foodfactory.dx.dto.CompleteBatchRequest;
import com.foodfactory.dx.dto.CreateBatchRequest;
import com.foodfactory.dx.dto.FefoAllocationResult;
import com.foodfactory.dx.dto.RejectBatchRequest;
import com.foodfactory.dx.service.ManufacturingService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ManufacturingBatchController {

    private final ManufacturingService manufacturingService;

    public ManufacturingBatchController(ManufacturingService manufacturingService) {
        this.manufacturingService = manufacturingService;
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

    /** DRAFT → PLAN。バッチの内容を確定する。 */
    @PostMapping("/api/batches/{batchId}/confirm-plan")
    public ResponseEntity<Void> confirmPlan(@PathVariable Long batchId) {
        manufacturingService.confirmPlan(batchId);
        return ResponseEntity.ok().build();
    }

    /** DRAFT/PLAN → CANCELLED。製造開始前にバッチを取り消す(MRPが即座に再計算される)。 */
    @PostMapping("/api/batches/{batchId}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long batchId,
            @RequestBody CancelBatchRequest request) {
        manufacturingService.cancelBatch(batchId, request.getCancelComment());
        return ResponseEntity.ok().build();
    }

    /** PLAN → MANUFACTURING。作業員が実測した使用量をもとに製造を実行する。 */
    @PostMapping("/api/batches/{batchId}/execute")
    public ResponseEntity<Void> execute(
            @PathVariable Long batchId,
            @RequestBody List<ActualUsageInput> actualUsages) {
        manufacturingService.executeBatch(batchId, actualUsages);
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
