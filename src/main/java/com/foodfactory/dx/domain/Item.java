package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品マスタ(テーブル名: items)に対応するJavaオブジェクト。
 */
public class Item {

    private Long itemId;

    private String name;

    private BigDecimal safetyStockQty;

    private BigDecimal targetStockQty;

    private BigDecimal standardBatchQty;

    private Integer shelfLifeDays;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Item() {
    }

    public Item(String name, BigDecimal safetyStockQty, BigDecimal targetStockQty,
                BigDecimal standardBatchQty, Integer shelfLifeDays) {
        this.name = name;
        this.safetyStockQty = safetyStockQty;
        this.targetStockQty = targetStockQty;
        this.standardBatchQty = standardBatchQty;
        this.shelfLifeDays = shelfLifeDays;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getSafetyStockQty() {
        return safetyStockQty;
    }

    public void setSafetyStockQty(BigDecimal safetyStockQty) {
        this.safetyStockQty = safetyStockQty;
    }

    public BigDecimal getTargetStockQty() {
        return targetStockQty;
    }

    public void setTargetStockQty(BigDecimal targetStockQty) {
        this.targetStockQty = targetStockQty;
    }

    public BigDecimal getStandardBatchQty() {
        return standardBatchQty;
    }

    public void setStandardBatchQty(BigDecimal standardBatchQty) {
        this.standardBatchQty = standardBatchQty;
    }

    public Integer getShelfLifeDays() {
        return shelfLifeDays;
    }

    public void setShelfLifeDays(Integer shelfLifeDays) {
        this.shelfLifeDays = shelfLifeDays;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
