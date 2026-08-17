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

    // 加水率・加水量の基準値(幅)。職人が試作を重ねて確立した、季節変動を織り込んだ範囲。
    // レシピ全体に対して1組だけ持つ(材料明細ごとではない)。
    // 製造実行画面で「加水基準値: 〇〇%〜〇〇% : 〇〇ml〜〇〇ml」というガイド表示として使う想定。
    private BigDecimal hydrationRatioMin;   // 加水率の下限(%)
    private BigDecimal hydrationRatioMax;   // 加水率の上限(%)
    private BigDecimal hydrationQtyMin;     // 加水量(溶液合計)の下限(ml)。主原料の使用量×加水率で算出した参考値
    private BigDecimal hydrationQtyMax;     // 加水量(溶液合計)の上限(ml)

    private boolean active = true;          // 有効/廃版フラグ。物理削除はせず、廃版になったらfalseにする(論理削除)
    private LocalDateTime createdAt;        // 登録日時(DB側で自動設定)
    private LocalDateTime updatedAt;        // 更新日時(DB側で自動設定)

    public Item() {
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

    public BigDecimal getHydrationRatioMin() {
        return hydrationRatioMin;
    }

    public void setHydrationRatioMin(BigDecimal hydrationRatioMin) {
        this.hydrationRatioMin = hydrationRatioMin;
    }

    public BigDecimal getHydrationRatioMax() {
        return hydrationRatioMax;
    }

    public void setHydrationRatioMax(BigDecimal hydrationRatioMax) {
        this.hydrationRatioMax = hydrationRatioMax;
    }

    public BigDecimal getHydrationQtyMin() {
        return hydrationQtyMin;
    }

    public void setHydrationQtyMin(BigDecimal hydrationQtyMin) {
        this.hydrationQtyMin = hydrationQtyMin;
    }

    public BigDecimal getHydrationQtyMax() {
        return hydrationQtyMax;
    }

    public void setHydrationQtyMax(BigDecimal hydrationQtyMax) {
        this.hydrationQtyMax = hydrationQtyMax;
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
