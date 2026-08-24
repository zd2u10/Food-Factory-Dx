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

    /** 製造実行画面(FEFO)の「破棄する」操作による調整の場合の理由。それ以外の調整ではnull。 */
    public enum UsageDiscardReason {
        MIXING_MISTAKE,
        MATERIAL_DEFECT,
        CONTAMINATION,
        OTHER
    }

    /**
     * 在庫調整画面(検査結果登録)からの調整の場合の理由。それ以外の調整ではnull。
     *
     * 【設計意図】理由をUsageDiscardReason(FEFO画面用)と、この
     * StockReviewReason(在庫調整画面用)の2つのenumに分けているのは、
     * 一方の画面で選ぶはずのない理由(期限切れのロットはFEFO候補に出てこないため
     * 選ばれ得ない、配合ミスは在庫調整の文脈では起こらない、等)が、
     * 選択肢に混在しないようにするため(要件定義書8.22節を参照)。
     */
    public enum StockReviewReason {
        EXPIRED,
        STORAGE_ISSUE,
        CONTAMINATION,
        OTHER
    }

    private Long adjustmentId;          // 主キー
    private Long lotId;                 // どの材料ロットの調整か
    private BigDecimal beforeQty;       // 調整前の残量
    private BigDecimal afterQty;        // 調整後の残量
    private LocalDate adjustmentDate;   // 調整日
    private UsageDiscardReason usageDiscardReason; // FEFO画面「破棄する」の理由。stockReviewReasonとは排他的
    private StockReviewReason stockReviewReason;   // 在庫調整画面の理由。usageDiscardReasonとは排他的
    private String comment;             // 調整理由の詳細(必須。理由がOTHERの場合は具体的な内容を必ず記入する)
    private LocalDateTime createdAt;    // 登録日時(DB側で自動設定)
    private LocalDateTime updatedAt;    // 更新日時(DB側で自動設定)

    public StockAdjustment() {
    }

    /** 理由(usageDiscardReason/stockReviewReasonどちらも)が無い調整(結局受け入れ等)用のコンストラクタ。 */
    public StockAdjustment(Long lotId, BigDecimal beforeQty, BigDecimal afterQty,
                            LocalDate adjustmentDate, String comment) {
        this.lotId = lotId;
        this.beforeQty = beforeQty;
        this.afterQty = afterQty;
        this.adjustmentDate = adjustmentDate;
        this.comment = comment;
    }

    /** FEFO画面「破棄する」操作用のコンストラクタ。 */
    public static StockAdjustment forUsageDiscard(Long lotId, BigDecimal beforeQty, BigDecimal afterQty,
                                                   LocalDate adjustmentDate, UsageDiscardReason reason, String comment) {
        StockAdjustment adjustment = new StockAdjustment(lotId, beforeQty, afterQty, adjustmentDate, comment);
        adjustment.usageDiscardReason = reason;
        return adjustment;
    }

    /** 在庫調整画面(検査結果登録)用のコンストラクタ。 */
    public static StockAdjustment forStockReview(Long lotId, BigDecimal beforeQty, BigDecimal afterQty,
                                                  LocalDate adjustmentDate, StockReviewReason reason, String comment) {
        StockAdjustment adjustment = new StockAdjustment(lotId, beforeQty, afterQty, adjustmentDate, comment);
        adjustment.stockReviewReason = reason;
        return adjustment;
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

    public UsageDiscardReason getUsageDiscardReason() {
        return usageDiscardReason;
    }

    public void setUsageDiscardReason(UsageDiscardReason usageDiscardReason) {
        this.usageDiscardReason = usageDiscardReason;
    }

    public StockReviewReason getStockReviewReason() {
        return stockReviewReason;
    }

    public void setStockReviewReason(StockReviewReason stockReviewReason) {
        this.stockReviewReason = stockReviewReason;
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
