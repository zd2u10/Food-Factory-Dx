package com.foodfactory.dx.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品マスタ(テーブル名: items)
 */
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "safety_stock_qty", nullable = false, precision = 10, scale = 2)
    private BigDecimal safetyStockQty;

    @Column(name = "target_stock_qty", nullable = false, precision = 10, scale = 2)
    private BigDecimal targetStockQty;

    @Column(name = "standard_batch_qty", nullable = false, precision = 10, scale = 2)
    private BigDecimal standardBatchQty;

    @Column(name = "shelf_life_days", nullable = false)
    private Integer shelfLifeDays;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected Item() {
        // JPA用
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
