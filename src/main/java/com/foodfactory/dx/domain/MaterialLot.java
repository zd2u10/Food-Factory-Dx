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

    private Long lotId;               // 主キー。材料版の「ロットID」に相当
    private Long materialId;          // どの材料のロットか
    private Long arrivalLineId;       // 生成元となった入荷明細(トレーサビリティの核となる紐付け)
    private String supplierLotNo;     // 仕入先発行のロット番号(arrival_lineからコピー。ここで新規採番はしない)
    private String origin;            // 産地
    private LocalDate expiryDate;      // 賞味期限(FEFO判定の基準)
    private BigDecimal remainingQty;   // 残量。消費・廃棄のたびにDB側で条件付き引き算される
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
