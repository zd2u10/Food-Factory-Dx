package com.foodfactory.dx.dto;

/** POST /api/batches/{batchId}/reject のリクエストボディ用DTO。 */
public class RejectBatchRequest {

    private String rejectComment;

    public RejectBatchRequest() {
    }

    public String getRejectComment() {
        return rejectComment;
    }

    public void setRejectComment(String rejectComment) {
        this.rejectComment = rejectComment;
    }
}
