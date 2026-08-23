package com.foodfactory.dx.dto;

import java.math.BigDecimal;
import java.util.List;

/** POST /api/batches/{batchId}/execute のリクエストボディ用DTO。 */
public class ExecuteBatchRequest {

    private List<ActualUsageInput> actualUsages;
    private BigDecimal actualHydrationQty; // 実際に加えた水の実測量(ml)。任意項目(加水が無いレシピもあるため)

    public ExecuteBatchRequest() {
    }

    public List<ActualUsageInput> getActualUsages() {
        return actualUsages;
    }

    public void setActualUsages(List<ActualUsageInput> actualUsages) {
        this.actualUsages = actualUsages;
    }

    public BigDecimal getActualHydrationQty() {
        return actualHydrationQty;
    }

    public void setActualHydrationQty(BigDecimal actualHydrationQty) {
        this.actualHydrationQty = actualHydrationQty;
    }
}
