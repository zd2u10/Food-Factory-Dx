package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 在庫の手動調整記録。
 * material_lot.remainingQty を直接書き換えるのではなく、必ずこのテーブルを経由して
 * 変更履歴(調整前・調整後・理由)を残す運用にする(監査要件対応)。
 * 「結局受け入れる(ACCEPTED_LATE)」による在庫増加も、このテーブルに記録してから反映する。
 */
public class StockAdjustment {

    private Long adjustmentId;          // 主キー
    private Long lotId;                 // どの材料ロットの調整か
    private BigDecimal beforeQty;       // 調整前の残量
    private BigDecimal afterQty;        // 調整後の残量
    private LocalDate adjustmentDate;   // 調整日
    private String comment;             // 調整理由(必須)
    private LocalDateTime createdAt;    // 登録日時(DB側で自動設定)
    private LocalDateTime updatedAt;    // 更新日時(DB側で自動設定)

    public StockAdjustment() {
    }

    public StockAdjustment(Long lotId, BigDecimal beforeQty, BigDecimal afterQty,
                            LocalDate adjustmentDate, String comment) {
        this.lotId = lotId;
        this.beforeQty = beforeQty;
        this.afterQty = afterQty;
        this.adjustmentDate = adjustmentDate;
        this.comment = comment;
    }

    public Long getAdjustmentId() {
        return adjustmentId;
    }

    public void setAdjustmentId(Long adjustmentId) {
        this.adjustmentId = adjustmentId;
    }

    public Long getLotId() {
        return lotId;
    }

    public void setLotId(Long lotId) {
        this.lotId = lotId;
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
