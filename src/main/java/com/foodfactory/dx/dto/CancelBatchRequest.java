package com.foodfactory.dx.dto;

/** POST /api/batches/{batchId}/cancel のリクエストボディ用DTO。 */
public class CancelBatchRequest {

    private String cancelComment;

    public CancelBatchRequest() {
    }

    public String getCancelComment() {
        return cancelComment;
    }

    public void setCancelComment(String cancelComment) {
        this.cancelComment = cancelComment;
    }
}
