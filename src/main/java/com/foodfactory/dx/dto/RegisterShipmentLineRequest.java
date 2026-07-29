package com.foodfactory.dx.dto;

import java.util.List;

/** POST /api/shipments/{shipmentId}/lines のリクエストボディ用DTO。 */
public class RegisterShipmentLineRequest {

    private Long orderLineId;
    private List<BatchAllocationInput> allocations;

    public RegisterShipmentLineRequest() {
    }

    public Long getOrderLineId() {
        return orderLineId;
    }

    public void setOrderLineId(Long orderLineId) {
        this.orderLineId = orderLineId;
    }

    public List<BatchAllocationInput> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<BatchAllocationInput> allocations) {
        this.allocations = allocations;
    }
}
