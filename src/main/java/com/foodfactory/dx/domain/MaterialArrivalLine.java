package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入荷明細(ロット単位)。
 * 検品(破損・期限切れ・異物混入)はこの単位で行い、
 * 同じ明細内でも「合格した数量」と「保留になった数量」を分けて記録する。
 */
public class MaterialArrivalLine {

    private Long lineId;                       // 主キー
    private Long arrivalId;                     // どの入荷ヘッダー(伝票)に属するか
    private String supplierLotNo;               // 仕入先が発行したロット番号(人が手入力する値)
    private String origin;                      // 産地
    private LocalDate expiryDate;                // 賞味期限(FEFO判定の基準になる)
    private Integer packageCount;                // 入荷した箱数/袋数
    private BigDecimal packageWeightSnapshot;    // 入荷時点での1箱あたり目安重量のスナップショット
    private BigDecimal arrivedQty;                // 総量。packageCount×packageWeightSnapshotで自動計算
    private BigDecimal acceptedQty;               // 検品合格数量(在庫に反映される分)
    private BigDecimal heldQty;                   // 検品保留数量(在庫には反映しない分)

    // 検品項目。true = 問題なし(合格)、false = 問題ありという向きに統一している。
    private boolean checkDamage;         // 破損がないか
    private boolean checkExpiry;         // 期限切れでないか
    private boolean checkContamination;  // 異物混入の兆候がないか

    private Long exchangeSourceLineId;   // 交換品として登録された場合、元の保留明細を参照(自己参照。新規入荷ならnull)
    private LocalDateTime createdAt;     // 登録日時(DB側で自動設定)
    private LocalDateTime updatedAt;     // 更新日時(DB側で自動設定)

    public MaterialArrivalLine() {
    }

    public Long getLineId() {
        return lineId;
    }

    public void setLineId(Long lineId) {
        this.lineId = lineId;
    }

    public Long getArrivalId() {
        return arrivalId;
    }

    public void setArrivalId(Long arrivalId) {
        this.arrivalId = arrivalId;
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

    public Integer getPackageCount() {
        return packageCount;
    }

    public void setPackageCount(Integer packageCount) {
        this.packageCount = packageCount;
    }

    public BigDecimal getPackageWeightSnapshot() {
        return packageWeightSnapshot;
    }

    public void setPackageWeightSnapshot(BigDecimal packageWeightSnapshot) {
        this.packageWeightSnapshot = packageWeightSnapshot;
    }

    public BigDecimal getArrivedQty() {
        return arrivedQty;
    }

    public void setArrivedQty(BigDecimal arrivedQty) {
        this.arrivedQty = arrivedQty;
    }

    public BigDecimal getAcceptedQty() {
        return acceptedQty;
    }

    public void setAcceptedQty(BigDecimal acceptedQty) {
        this.acceptedQty = acceptedQty;
    }

    public BigDecimal getHeldQty() {
        return heldQty;
    }

    public void setHeldQty(BigDecimal heldQty) {
        this.heldQty = heldQty;
    }

    public boolean isCheckDamage() {
        return checkDamage;
    }

    public void setCheckDamage(boolean checkDamage) {
        this.checkDamage = checkDamage;
    }

    public boolean isCheckExpiry() {
        return checkExpiry;
    }

    public void setCheckExpiry(boolean checkExpiry) {
        this.checkExpiry = checkExpiry;
    }

    public boolean isCheckContamination() {
        return checkContamination;
    }

    public void setCheckContamination(boolean checkContamination) {
        this.checkContamination = checkContamination;
    }

    public Long getExchangeSourceLineId() {
        return exchangeSourceLineId;
    }

    public void setExchangeSourceLineId(Long exchangeSourceLineId) {
        this.exchangeSourceLineId = exchangeSourceLineId;
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
