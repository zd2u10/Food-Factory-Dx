package com.foodfactory.dx.dto;

import com.foodfactory.dx.domain.StockAdjustment;
import java.math.BigDecimal;

/** POST /api/material-lots/{lotId}/discard-usage のリクエストボディ用DTO。 */
public class DiscardUsageRequest {

    private BigDecimal discardQty; // 廃棄する量(製造実行画面で入力していた実測値、そのまま)
    private StockAdjustment.UsageDiscardReason reason;
    private String comment;

    public DiscardUsageRequest() {
    }

    public BigDecimal getDiscardQty() {
        return discardQty;
    }

    public void setDiscardQty(BigDecimal discardQty) {
        this.discardQty = discardQty;
    }

    public StockAdjustment.UsageDiscardReason getReason() {
        return reason;
    }

    public void setReason(StockAdjustment.UsageDiscardReason reason) {
        this.reason = reason;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
