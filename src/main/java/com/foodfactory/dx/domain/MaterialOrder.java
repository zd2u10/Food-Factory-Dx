package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 発注記録(発注ヘッダー)。
 * 「材料を何g/ml発注したか」だけを表し、実際に届いたかどうかは material_arrival 側で管理する。
 */
public class MaterialOrder {

    public enum Status {
        NOT_ARRIVED,        // 未入荷(まだ何も届いていない)
        PARTIALLY_ARRIVED,  // 一部入荷(発注数量の一部だけ合格済み)
        FULLY_ARRIVED       // 入荷完了(発注数量分すべて合格済み)
    }

    private Long orderId;             // 主キー
    private Long materialId;          // 発注対象の材料(material.materialIdを参照)
    private String supplierId;        // 仕入先(現状は文字列管理。将来supplierマスタに分離してもよい)
    private BigDecimal orderQty;      // 発注数量(g または ml)
    private String allowedOrigins;    // この発注で許可する産地をカンマ区切りで保持(例: "愛知,三重")。
                                       // recipe_item.allowedOriginsと同じ形式。任意項目(null許容)。
    private LocalDate orderDate;      // 発注日
    private LocalDate expectedDate;   // 納品予定日(仕入先から明言されないこともあるためnull許容)

    // フィールド宣言時点で初期値を持たせている(理由は下記コメント参照)。
    private Status status = Status.NOT_ARRIVED; // 発注の充足状況。Service層が入荷実績から自動算出・更新する

    private LocalDateTime createdAt;  // 登録日時(DB側で自動設定)
    private LocalDateTime updatedAt;  // 更新日時(DB側で自動設定)

    public MaterialOrder() {
    }

    public MaterialOrder(Long materialId, String supplierId, BigDecimal orderQty,
                          LocalDate orderDate, LocalDate expectedDate) {
        this.materialId = materialId;
        this.supplierId = supplierId;
        this.orderQty = orderQty;
        this.orderDate = orderDate;
        this.expectedDate = expectedDate;
        // statusはフィールド宣言時点で既にNOT_ARRIVEDになっているため、ここでの再設定は不要。
        // (JacksonがJSON→Javaオブジェクト変換時に引数なしコンストラクタ経由で作るため、
        //  この引数付きコンストラクタは実際にはAPI経由では呼ばれない。詳細は下記参照)
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

    public BigDecimal getOrderQty() {
        return orderQty;
    }

    public void setOrderQty(BigDecimal orderQty) {
        this.orderQty = orderQty;
    }

    public String getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(LocalDate expectedDate) {
        this.expectedDate = expectedDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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
