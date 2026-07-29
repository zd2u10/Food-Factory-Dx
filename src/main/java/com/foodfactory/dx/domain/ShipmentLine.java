package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出荷明細。
 * order_line(受注明細)と manufacturing_batch(商品ロット)を仲介する多対多の中間テーブル。
 * 「1つの受注明細が複数バッチにまたがる」「1回の出荷が複数バッチ・複数受注にまたがる」の
 * 両方に対応するため、この形にしている(材料側のbatch_material_usageと同じ考え方)。
 */
public class ShipmentLine {

    private Long lineId;             // 主キー
    private Long shipmentId;         // どの出荷ヘッダー(配送イベント)に属するか
    private Long orderLineId;        // どの受注明細に対する出荷か
    private Long batchId;            // 出荷元の製造バッチ(=商品ロット)
    private BigDecimal shippedQty;   // 出荷数量
    private LocalDateTime createdAt; // 登録日時(DB側で自動設定)
    private LocalDateTime updatedAt; // 更新日時(DB側で自動設定)

    public ShipmentLine() {
    }

    public ShipmentLine(Long shipmentId, Long orderLineId, Long batchId, BigDecimal shippedQty) {
        this.shipmentId = shipmentId;
        this.orderLineId = orderLineId;
        this.batchId = batchId;
        this.shippedQty = shippedQty;
    }

    public Long getLineId() {
        return lineId;
    }

    public void setLineId(Long lineId) {
        this.lineId = lineId;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    public Long getOrderLineId() {
        return orderLineId;
    }

    public void setOrderLineId(Long orderLineId) {
        this.orderLineId = orderLineId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public BigDecimal getShippedQty() {
        return shippedQty;
    }

    public void setShippedQty(BigDecimal shippedQty) {
        this.shippedQty = shippedQty;
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
