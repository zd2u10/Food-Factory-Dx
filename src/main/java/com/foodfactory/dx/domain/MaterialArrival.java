package com.foodfactory.dx.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入荷ヘッダー(伝票1枚 = 1回の配送イベント)。
 * 実際のロット情報(産地・賞味期限・数量)はここではなく、
 * このヘッダーに複数ぶら下がる material_arrival_line 側が持つ。
 */
public class MaterialArrival {

    private Long arrivalId;    // 主キー
    private Long orderId;      // 対応する発注(緊急入荷等、発注に紐づかない場合はnull)
    private Long materialId;   // 入荷した材料。発注に紐づく場合は発注側の値がService層で自動コピーされる
    private String supplierId; // 仕入先
    private LocalDate arrivalDate;   // 入荷日(伝票の日付)
    private LocalDateTime createdAt; // 登録日時(DB側で自動設定)
    private LocalDateTime updatedAt; // 更新日時(DB側で自動設定)

    public MaterialArrival() {
    }

    public MaterialArrival(Long orderId, Long materialId, String supplierId, LocalDate arrivalDate) {
        this.orderId = orderId;
        this.materialId = materialId;
        this.supplierId = supplierId;
        this.arrivalDate = arrivalDate;
    }

    public Long getArrivalId() {
        return arrivalId;
    }

    public void setArrivalId(Long arrivalId) {
        this.arrivalId = arrivalId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
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
