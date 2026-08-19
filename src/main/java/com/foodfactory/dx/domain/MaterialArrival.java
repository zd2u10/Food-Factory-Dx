package com.foodfactory.dx.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入荷ヘッダー(伝票1枚 = 1回の配送イベント)。
 * 1回の配送で複数の異なる材料・複数の異なる発注がまとめて届くことがあるため、
 * materialId/orderIdはこのヘッダーではなく、明細(MaterialArrivalLine)側に持たせる。
 * ヘッダーは「いつ・どの仕入先から届いたか」という配送イベントの情報だけを持つ。
 */
public class MaterialArrival {

    private Long arrivalId;          // 主キー
    private Long supplierId;         // 仕入先(supplier.supplierIdを参照。旧supplier_id列(文字列)からの
                                      // 移行に伴い、DB上はsupplier_ref_id列にマッピングされる)
    private LocalDate arrivalDate;   // 入荷日(伝票の日付)
    private LocalDateTime createdAt; // 登録日時(DB側で自動設定)
    private LocalDateTime updatedAt; // 更新日時(DB側で自動設定)

    public MaterialArrival() {
    }

    public MaterialArrival(Long supplierId, LocalDate arrivalDate) {
        this.supplierId = supplierId;
        this.arrivalDate = arrivalDate;
    }

    public Long getArrivalId() {
        return arrivalId;
    }

    public void setArrivalId(Long arrivalId) {
        this.arrivalId = arrivalId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
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
