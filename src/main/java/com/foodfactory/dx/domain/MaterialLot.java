package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 材料ロット(在庫の実体)。
 * 入荷明細(MaterialArrivalLine)が検品合格になったタイミングで、
 * Service層の処理により自動的に1件生成される。
 */
public class MaterialLot {

    /** 要確認になった理由。配合ミス/保管ミス/異物混入/その他。 */
    /**
     * 「別ロットに切り替える」操作を行った理由。ロット自体(1袋全体)を疑うケースのみを
     * 想定しており、「配合ミス」(投入分だけの問題)はここには含めない
     * (配合ミスは、同一ロットのまま完結する「破棄する」操作(UsageDiscardReason)側の理由)。
     */
    public enum ReviewReason {
        STORAGE_ISSUE,
        CONTAMINATION,
        OTHER
    }

    private Long lotId;               // 主キー。材料版の「ロットID」に相当
    private Long materialId;          // どの材料のロットか
    private Long arrivalLineId;       // 生成元となった入荷明細(トレーサビリティの核となる紐付け)
    private Long originHoldId;        // このロットが「結局受け入れ」(ACCEPTED_LATE)によって生成された場合、
                                       // 元になったhold_resolution.hold_idを記録する。
                                       // 通常の入荷で作られたロットはnullのまま。
                                       // 「普通に合格した分」と「一度保留を経て受け入れた分」を
                                       // ロット単位で区別するための項目
    private String supplierLotNo;     // 仕入先発行のロット番号(arrival_lineからコピー。ここで新規採番はしない)
    private String origin;            // 産地
    private LocalDate expiryDate;      // 賞味期限(FEFO判定の基準)
    private BigDecimal remainingQty;   // 残量。消費・廃棄のたびにDB側で条件付き引き算される
    private boolean needsReview;       // 製造実行画面で「別ロットに切り替える」操作が行われた場合にtrue。
                                        // trueのロットはFEFO自動選定の対象から除外される。
                                        // 人が検査結果を登録するまで、remainingQty自体は変更しない
    private ReviewReason reviewReason; // 要確認になった理由
    private String reviewComment;      // 理由がOTHERの場合の自由記述、または補足コメント
    private LocalDateTime createdAt;   // 登録日時(DB側で自動設定)
    private LocalDateTime updatedAt;   // 更新日時(DB側で自動設定)

    public MaterialLot() {
    }

    public MaterialLot(Long materialId, Long arrivalLineId, String supplierLotNo, String origin,
                        LocalDate expiryDate, BigDecimal remainingQty) {
        this.materialId = materialId;
        this.arrivalLineId = arrivalLineId;
        this.supplierLotNo = supplierLotNo;
        this.origin = origin;
        this.expiryDate = expiryDate;
        this.remainingQty = remainingQty;
    }

    public Long getLotId() {
        return lotId;
    }

    public void setLotId(Long lotId) {
        this.lotId = lotId;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public Long getArrivalLineId() {
        return arrivalLineId;
    }

    public void setArrivalLineId(Long arrivalLineId) {
        this.arrivalLineId = arrivalLineId;
    }

    public Long getOriginHoldId() {
        return originHoldId;
    }

    public void setOriginHoldId(Long originHoldId) {
        this.originHoldId = originHoldId;
    }

    public String getSupplierLotNo() {
        return supplierLotNo;
    }

    public void setSupplierLotNo(String supplierLotNo) {
        this.supplierLotNo = supplierLotNo;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public BigDecimal getRemainingQty() {
        return remainingQty;
    }

    public void setRemainingQty(BigDecimal remainingQty) {
        this.remainingQty = remainingQty;
    }

    public boolean isNeedsReview() {
        return needsReview;
    }

    public void setNeedsReview(boolean needsReview) {
        this.needsReview = needsReview;
    }

    public ReviewReason getReviewReason() {
        return reviewReason;
    }

    public void setReviewReason(ReviewReason reviewReason) {
        this.reviewReason = reviewReason;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
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
