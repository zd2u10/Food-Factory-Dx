package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入荷明細(ロット単位)。
 * 検品(破損・期限切れ・異物混入)はこの単位で行い、
 * 同じ明細内でも「合格した数量」と「保留になった数量」を分けて記録する。
 *
 * 例: 5箱届いたうち3箱は合格、2箱は不合格 →
 *     acceptedQty = 3箱分の重量、heldQty = 2箱分の重量、として1件の明細にまとめて記録する。
 */
public class MaterialArrivalLine {

    private Long lineId;
    private Long arrivalId;
    private String supplierLotNo;
    private String origin;
    private LocalDate expiryDate;
    private Integer packageCount;
    private BigDecimal packageWeightSnapshot;
    private BigDecimal arrivedQty;
    private BigDecimal acceptedQty;
    private BigDecimal heldQty;

    // 検品項目。true = 問題なし(合格)、false = 問題あり、という向きに統一している。
    // (「異常があったらtrue」のような逆向きの意味にすると、コードを読む際に混乱しやすいため)
    private boolean checkDamage;
    private boolean checkExpiry;
    private boolean checkContamination;

    // 交換品として登録された明細の場合、元になった保留明細のIDをここに持つ(自己参照)。
    // 新規の入荷の場合はnullのまま。
    private Long exchangeSourceLineId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
