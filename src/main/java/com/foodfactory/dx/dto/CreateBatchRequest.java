package com.foodfactory.dx.dto;

import java.time.LocalDate;

/** POST /api/items/{itemId}/batches のリクエストボディ用DTO。 */
public class CreateBatchRequest {

    private LocalDate batchDate;
    private String createdBy;

    public CreateBatchRequest() {
    }

    public LocalDate getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(LocalDate batchDate) {
        this.batchDate = batchDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
