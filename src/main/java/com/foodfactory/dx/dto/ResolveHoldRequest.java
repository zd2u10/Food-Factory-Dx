package com.foodfactory.dx.dto;

/** POST /api/holds/{holdId}/resolve-* のリクエストボディ用DTO。 */
public class ResolveHoldRequest {

    private String comment;

    public ResolveHoldRequest() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
