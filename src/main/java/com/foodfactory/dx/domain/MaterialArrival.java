package com.foodfactory.dx.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入荷ヘッダー(伝票1枚 = 1回の配送イベント)。
 * 実際のロット情報(産地・賞味期限・数量)はここではなく、
 * このヘッダーに複数ぶら下がる material_arrival_line 側が持つ。
 * (1回の配送の中に、産地・期限違いの複数ロットが混在することがあるため)
 */
public class MaterialArrival {

    private Long arrivalId;

    // 発注に紐づかない緊急入荷もあり得るため、Long(ラッパー型)にしてnullを許容する。
    // long(プリミティブ型)にしてしまうと、nullを表現できずコンパイルエラーになる。
    private Long orderId;

    // どの材料の入荷かを必ず特定できるようにするための項目。
    // 発注に紐づく通常入荷であれば発注側のmaterialIdと同じ値になるが、
    // 緊急入荷(orderIdがnull)の場合はこちらに直接値を設定する。
    private Long materialId;

    private String supplierId;
    private LocalDate arrivalDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
