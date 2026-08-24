package com.foodfactory.dx.dto;

import com.foodfactory.dx.domain.MaterialLot;

/** POST /api/material-lots/{lotId}/mark-needs-review のリクエストボディ用DTO。 */
public class MarkNeedsReviewRequest {

    private MaterialLot.ReviewReason reviewReason;
    private String reviewComment;

    public MarkNeedsReviewRequest() {
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
