package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.StockAdjustment;
import com.foodfactory.dx.dto.AdjustStockRequest;
import com.foodfactory.dx.service.StockAdjustmentService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/material-lots/{lotId}/adjustments")
public class StockAdjustmentController {

    private final StockAdjustmentService stockAdjustmentService;

    public StockAdjustmentController(StockAdjustmentService stockAdjustmentService) {
        this.stockAdjustmentService = stockAdjustmentService;
    }

    /** 指定したロットの調整履歴を全件取得する。 */
    @GetMapping
    public List<StockAdjustment> list(@PathVariable Long lotId) {
        return stockAdjustmentService.listByLotId(lotId);
    }

    /** ロットの残量を、実測した数量に補正する(理由コメント必須)。 */
    @PostMapping
    public ResponseEntity<StockAdjustment> adjust(
            @PathVariable Long lotId,
            @RequestBody AdjustStockRequest request) {
        StockAdjustment adjustment = stockAdjustmentService.adjustLotQuantity(
                lotId, request.getNewQty(), request.getComment());
        return ResponseEntity.status(HttpStatus.CREATED).body(adjustment);
    }
}
