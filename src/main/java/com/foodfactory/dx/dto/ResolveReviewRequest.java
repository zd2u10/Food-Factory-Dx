package com.foodfactory.dx.dto;

import com.foodfactory.dx.domain.StockAdjustment;
import java.math.BigDecimal;

/** POST /api/material-lots/{lotId}/resolve-review のリクエストボディ用DTO。 */
public class ResolveReviewRequest {

    private BigDecimal survivingQty; // 生存量。0を指定すれば全量破棄と同じ結果になる
    private StockAdjustment.StockReviewReason reason;
    private String comment;

    public ResolveReviewRequest() {
    }

    public BigDecimal getSurvivingQty() {
        return survivingQty;
    }

    public void setSurvivingQty(BigDecimal survivingQty) {
        this.survivingQty = survivingQty;
    }

    public StockAdjustment.StockReviewReason getReason() {
        return reason;
    }

    public void setReason(StockAdjustment.StockReviewReason reason) {
        this.reason = reason;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
