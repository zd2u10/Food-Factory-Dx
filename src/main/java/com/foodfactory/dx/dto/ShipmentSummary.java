package com.foodfactory.dx.dto;

import com.foodfactory.dx.domain.Shipment;
import java.time.LocalDate;
import java.util.List;

/**
 * 出荷一覧画面の表示用DTO。出荷ヘッダー自体には受注IDが直接無い
 * (shipment_line → order_line経由でしか辿れない)ため、一覧で毎回JOINするより、
 * ここで一度だけ集計してまとめて返す(要件定義書8.24節を参照)。
 *
 * 一覧では「出荷ID・受注ID・取引先」を主役として見せ、それ以外の項目
 * (配送会社・出荷日・配送先・温度帯)は、詳細表示でのみ見せる想定。
 */
public class ShipmentSummary {

    private Long shipmentId;
    private List<Long> orderIds; // 1つの出荷が複数の受注にまたがる可能性を考慮しListで持つ
    private String customerName; // 代表の取引先名(通常は1件、複数受注が混在する場合は先頭のもの)
    private Long carrierId;
    private LocalDate shippedDate;
    private String destination;
    private Shipment.TemperatureZone temperatureZone;

    public static ShipmentSummary from(Shipment shipment, List<Long> orderIds, String customerName) {
        ShipmentSummary summary = new ShipmentSummary();
        summary.shipmentId = shipment.getShipmentId();
        summary.orderIds = orderIds;
        summary.customerName = customerName;
        summary.carrierId = shipment.getCarrierId();
        summary.shippedDate = shipment.getShippedDate();
        summary.destination = shipment.getDestination();
        summary.temperatureZone = shipment.getTemperatureZone();
        return summary;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public List<Long> getOrderIds() {
        return orderIds;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Long getCarrierId() {
        return carrierId;
    }

    public LocalDate getShippedDate() {
        return shippedDate;
    }

    public String getDestination() {
        return destination;
    }

    public Shipment.TemperatureZone getTemperatureZone() {
        return temperatureZone;
    }
}
