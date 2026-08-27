package com.foodfactory.dx.dto;

import com.foodfactory.dx.domain.ItemStockAdjustment;
import java.math.BigDecimal;

/** POST /api/batches/{batchId}/discard-item-stock のリクエストボディ用DTO。 */
public class DiscardItemStockRequest {

    private BigDecimal discardQty;
    private ItemStockAdjustment.AdjustmentReason reason;
    private String comment;

    public DiscardItemStockRequest() {
    }

    public BigDecimal getDiscardQty() {
        return discardQty;
    }

    public void setDiscardQty(BigDecimal discardQty) {
        this.discardQty = discardQty;
    }

    public ItemStockAdjustment.AdjustmentReason getReason() {
        return reason;
    }

    public void setReason(ItemStockAdjustment.AdjustmentReason reason) {
        this.reason = reason;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
