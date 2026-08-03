package com.foodfactory.dx.dto;

import java.util.List;

/** POST /api/batches/confirm-plan-bulk のリクエストボディ用DTO。 */
public class ConfirmPlanBulkRequest {

    private List<Long> batchIds;

    public ConfirmPlanBulkRequest() {
    }

    public List<Long> getBatchIds() {
        return batchIds;
    }

    public void setBatchIds(List<Long> batchIds) {
        this.batchIds = batchIds;
    }
}
