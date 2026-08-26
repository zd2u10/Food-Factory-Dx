package com.foodfactory.dx.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 出荷一覧の「明細を見る」で表示する、1件の出荷明細の詳細情報。
 * shipment_line だけでは商品名・ロット番号までは分からないため、
 * order_line(商品ID)・manufacturing_batch(製造日・連番、ロット番号の組み立てに必要)を
 * まとめて1つのDTOとして返す(要件定義書8.24節を参照)。
 */
public class ShipmentLineDetail {

    private Long lineId;
    private Long itemId;
    private BigDecimal shippedQty;
    private Long batchId;
    private LocalDate batchDate;
    private Integer batchSeq;

    public ShipmentLineDetail() {
    }

    public ShipmentLineDetail(Long lineId, Long itemId, BigDecimal shippedQty,
                               Long batchId, LocalDate batchDate, Integer batchSeq) {
        this.lineId = lineId;
        this.itemId = itemId;
        this.shippedQty = shippedQty;
        this.batchId = batchId;
        this.batchDate = batchDate;
        this.batchSeq = batchSeq;
    }

    public Long getLineId() {
        return lineId;
    }

    public Long getItemId() {
        return itemId;
    }

    public BigDecimal getShippedQty() {
        return shippedQty;
    }

    public Long getBatchId() {
        return batchId;
    }

    public LocalDate getBatchDate() {
        return batchDate;
    }

    public Integer getBatchSeq() {
        return batchSeq;
    }
}
