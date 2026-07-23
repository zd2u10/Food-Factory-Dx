package com.foodfactory.dx.dto;

import java.math.BigDecimal;

/** POST /api/batches/{batchId}/complete のリクエストボディ用DTO。 */
public class CompleteBatchRequest {

    private BigDecimal acceptedQty;
    private BigDecimal lossQty;
    private String lossComment;

    public CompleteBatchRequest() {
    }

    public BigDecimal getAcceptedQty() {
        return acceptedQty;
    }

    public void setAcceptedQty(BigDecimal acceptedQty) {
        this.acceptedQty = acceptedQty;
    }

    public BigDecimal getLossQty() {
        return lossQty;
    }

    public void setLossQty(BigDecimal lossQty) {
        this.lossQty = lossQty;
    }

    public String getLossComment() {
        return lossComment;
    }

    public void setLossComment(String lossComment) {
        this.lossComment = lossComment;
    }
}
