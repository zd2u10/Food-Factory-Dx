package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商品在庫(manufacturing_batch、COMPLETED状態)に対する、手動調整記録。
 * material_lot向けのStockAdjustmentと同じ考え方で、remainingQtyを直接
 * 書き換えるのではなく、必ずこのテーブルを経由して変更履歴を残す。
 *
 * 対象範囲: COMPLETED(検品完了・在庫化済み)の商品ロットに対する、
 * バックヤード担当者による調整のみ(要件定義書8.25節を参照)。
 */
public class ItemStockAdjustment {

    /** 保管・取り扱い不良/期限切れ/その他。保管ミスと破損は、実務上の意味が薄いため統合している。 */
    public enum AdjustmentReason {
        STORAGE_HANDLING_ISSUE,
        EXPIRED,
        OTHER
    }

    private Long adjustmentId;
    private Long batchId;
    private BigDecimal beforeQty;
    private BigDecimal afterQty;
    private LocalDate adjustmentDate;
    private AdjustmentReason adjustmentReason;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ItemStockAdjustment() {
    }

    public ItemStockAdjustment(Long batchId, BigDecimal beforeQty, BigDecimal afterQty,
                                LocalDate adjustmentDate, AdjustmentReason adjustmentReason, String comment) {
        this.batchId = batchId;
        this.beforeQty = beforeQty;
        this.afterQty = afterQty;
        this.adjustmentDate = adjustmentDate;
        this.adjustmentReason = adjustmentReason;
        this.comment = comment;
    }

    public Long getAdjustmentId() {
        return adjustmentId;
    }

    public void setAdjustmentId(Long adjustmentId) {
        this.adjustmentId = adjustmentId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public BigDecimal getBeforeQty() {
        return beforeQty;
    }

    public void setBeforeQty(BigDecimal beforeQty) {
        this.beforeQty = beforeQty;
    }

    public BigDecimal getAfterQty() {
        return afterQty;
    }

    public void setAfterQty(BigDecimal afterQty) {
        this.afterQty = afterQty;
    }

    public LocalDate getAdjustmentDate() {
        return adjustmentDate;
    }

    public void setAdjustmentDate(LocalDate adjustmentDate) {
        this.adjustmentDate = adjustmentDate;
    }

    public AdjustmentReason getAdjustmentReason() {
        return adjustmentReason;
    }

    public void setAdjustmentReason(AdjustmentReason adjustmentReason) {
        this.adjustmentReason = adjustmentReason;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
