package com.foodfactory.dx.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 出荷ヘッダー(配送1回分)。 */
public class Shipment {

    public enum TemperatureZone {
        FROZEN,   // 冷凍
        AMBIENT   // 常温
    }

    private Long shipmentId;                 // 主キー
    private Long carrierId;                  // 配送を担当する配送会社
    private LocalDate shippedDate;           // 出荷日
    private String destination;              // 配送先住所等(自由記述)
    private TemperatureZone temperatureZone; // 冷凍 or 常温
    private LocalDateTime createdAt;         // 登録日時(DB側で自動設定)
    private LocalDateTime updatedAt;         // 更新日時(DB側で自動設定)

    public Shipment() {
    }

    public Shipment(Long carrierId, LocalDate shippedDate, String destination, TemperatureZone temperatureZone) {
        this.carrierId = carrierId;
        this.shippedDate = shippedDate;
        this.destination = destination;
        this.temperatureZone = temperatureZone;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    public Long getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(Long carrierId) {
        this.carrierId = carrierId;
    }

    public LocalDate getShippedDate() {
        return shippedDate;
    }

    public void setShippedDate(LocalDate shippedDate) {
        this.shippedDate = shippedDate;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public TemperatureZone getTemperatureZone() {
        return temperatureZone;
    }

    public void setTemperatureZone(TemperatureZone temperatureZone) {
        this.temperatureZone = temperatureZone;
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
