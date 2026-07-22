package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 材料ロット(在庫の実体)。
 * 入荷明細(MaterialArrivalLine)が検品合格になったタイミングで、
 * Service層の処理により自動的に1件生成される(このクラス自身はその生成ロジックを持たない)。
 *
 * remainingQty(残量)は、製造での消費や廃棄のたびに、Service層がSQLのUPDATEで減算していく。
 * FEFO(期限が近い順に使う)の判定は、この expiryDate を基準に行う。
 */
public class MaterialLot {

    private Long lotId;
    private Long materialId;

    // どの入荷明細から生まれたロットかを必ず記録する。
    // これにより「出荷した商品 → 使った材料ロット → 元の入荷明細(検品記録)」まで
    // 遡って追跡できる(トレーサビリティの核となる紐付け)。
    private Long arrivalLineId;

    // 仕入先が発行したロット番号そのもの。
    // これは material_arrival_line.supplierLotNo として既に人が手入力した値であり、
    // ここで新しく採番しているわけではなく、そのままコピーして持たせているだけ。
    // (arrivalLineIdを辿れば元の値は分かるが、FEFOでロットを選ぶ画面などで
    //  毎回JOINして取りに行かずに済むよう、表示の利便性のためにコピーを持たせている)
    private String supplierLotNo;

    private String origin;
    private LocalDate expiryDate;
    private BigDecimal remainingQty;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
