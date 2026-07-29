package com.foodfactory.dx.dto;

import java.math.BigDecimal;

/** 出荷登録のリクエストボディで使うDTO。「どのバッチから、いくつ出荷するか」を受け取る。 */
public class BatchAllocationInput {

    private Long batchId;
    private BigDecimal shippedQty;

    public BatchAllocationInput() {
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public BigDecimal getShippedQty() {
        return shippedQty;
    }

    public void setShippedQty(BigDecimal shippedQty) {
        this.shippedQty = shippedQty;
    }
}
