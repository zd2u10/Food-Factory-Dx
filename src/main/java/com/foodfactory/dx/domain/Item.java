package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品マスタ(テーブル名: items)に対応するJavaオブジェクト。
 */
public class Item {

    private Long itemId;                    // 主キー
    private String name;                    // 商品名
    private BigDecimal safetyStockQty;      // 適正在庫(これを下回るとMRPが製造を要求する基準値)
    private BigDecimal targetStockQty;      // 目標在庫(将来拡張用。現時点では未使用)
    private BigDecimal standardBatchQty;    // 1バッチあたりの標準製造数(季節変動込みの平均値)
    private Integer shelfLifeDays;          // 賞味期限日数(製造日からの日数。現状90日固定)
    private boolean active = true;          // 有効/廃版フラグ。物理削除はせず、廃版になったらfalseにする(論理削除)
    private LocalDateTime createdAt;        // 登録日時(DB側で自動設定)
    private LocalDateTime updatedAt;        // 更新日時(DB側で自動設定)

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
