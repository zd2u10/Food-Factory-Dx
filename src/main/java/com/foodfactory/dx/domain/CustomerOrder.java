package com.foodfactory.dx.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 受注ヘッダー。 */
public class CustomerOrder {

    public enum Status {
        NEW,               // 受注受付
        CONFIRMED,         // 確定
        PARTIALLY_SHIPPED, // 一部出荷
        COMPLETED,         // 出荷完了
        CANCELLED          // キャンセル
    }

    private Long orderId;                  // 主キー
    private Long customerId;               // 取引先(customer.customerIdを参照)
    private LocalDate orderDate;           // 受注日
    private LocalDate desiredDeliveryDate; // 希望納品日(任意)
    private Status status = Status.NEW;    // 受注の進行状況。フィールド宣言時点で初期値を持たせている(JSON変換時も必ずNEWから始まるようにするため)
    private String externalOrderNo;        // 先方の注文システム上の番号(あれば)
    private LocalDateTime createdAt;       // 登録日時(DB側で自動設定)
    private LocalDateTime updatedAt;       // 更新日時(DB側で自動設定)

    public CustomerOrder() {
    }

    public CustomerOrder(Long customerId, LocalDate orderDate, LocalDate desiredDeliveryDate, String externalOrderNo) {
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.desiredDeliveryDate = desiredDeliveryDate;
        this.externalOrderNo = externalOrderNo;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getDesiredDeliveryDate() {
        return desiredDeliveryDate;
    }

    public void setDesiredDeliveryDate(LocalDate desiredDeliveryDate) {
        this.desiredDeliveryDate = desiredDeliveryDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getExternalOrderNo() {
        return externalOrderNo;
    }

    public void setExternalOrderNo(String externalOrderNo) {
        this.externalOrderNo = externalOrderNo;
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
