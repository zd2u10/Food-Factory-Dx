package com.foodfactory.dx.dto;

import java.time.LocalDate;

/** POST /api/batches/{batchId}/assign-date のリクエストボディ用DTO。 */
public class AssignToDateRequest {

    private LocalDate batchDate;

    public AssignToDateRequest() {
    }

    public LocalDate getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(LocalDate batchDate) {
        this.batchDate = batchDate;
    }
}
