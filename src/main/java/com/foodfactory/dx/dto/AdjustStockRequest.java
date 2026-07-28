package com.foodfactory.dx.dto;

import java.math.BigDecimal;

/** POST /api/material-lots/{lotId}/adjustments のリクエストボディ用DTO。 */
public class AdjustStockRequest {

    private BigDecimal newQty;
    private String comment;

    public AdjustStockRequest() {
    }

    public BigDecimal getNewQty() {
        return newQty;
    }

    public void setNewQty(BigDecimal newQty) {
        this.newQty = newQty;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
