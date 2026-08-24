package com.foodfactory.dx.dto;

import com.foodfactory.dx.domain.MaterialLot;

/** POST /api/material-lots/{lotId}/switch のリクエストボディ用DTO。 */
public class SwitchLotRequest {

    private Long itemId; // どの商品の製造で使っていたか(レシピの必要量を再取得するために必要)
    private MaterialLot.ReviewReason reviewReason;
    private String reviewComment;

    public SwitchLotRequest() {
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public MaterialLot.ReviewReason getReviewReason() {
        return reviewReason;
    }

    public void setReviewReason(MaterialLot.ReviewReason reviewReason) {
        this.reviewReason = reviewReason;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }
}
