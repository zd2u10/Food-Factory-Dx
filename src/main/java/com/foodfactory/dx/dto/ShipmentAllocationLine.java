package com.foodfactory.dx.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 出荷時のFEFO自動選定の結果1件分(どのバッチから、いくつ出荷すべきか)。 */
public class ShipmentAllocationLine {

    private Long batchId;
    private LocalDate batchDate;
    private int residualDays; // このバッチの、出荷予定日時点での残存期限の日数
    private BigDecimal allocatedQty;

    public ShipmentAllocationLine() {
    }

    public ShipmentAllocationLine(Long batchId, LocalDate batchDate, int residualDays, BigDecimal allocatedQty) {
        this.batchId = batchId;
        this.batchDate = batchDate;
        this.residualDays = residualDays;
        this.allocatedQty = allocatedQty;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public LocalDate getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(LocalDate batchDate) {
        this.batchDate = batchDate;
    }

    public int getResidualDays() {
        return residualDays;
    }

    public void setResidualDays(int residualDays) {
        this.residualDays = residualDays;
    }

    public BigDecimal getAllocatedQty() {
        return allocatedQty;
    }

    public void setAllocatedQty(BigDecimal allocatedQty) {
        this.allocatedQty = allocatedQty;
    }
}
