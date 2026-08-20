package com.foodfactory.dx.dto;

import com.foodfactory.dx.domain.MaterialOrder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 発注一覧・発注詳細の画面表示用DTO。
 *
 * 【経緯】発注一覧では正しい充足数量が表示されていたが、発注詳細画面は別途
 * フロント側で material_arrival_line.accepted_qty の合計だけを計算しており、
 * 「結局受け入れ」(ACCEPTED_LATE)経由の合格分が反映されず、一覧と詳細で
 * 表示される数字が食い違う不具合があった。
 *
 * これを構造的に防ぐため、充足数量の計算は常にサーバー側で一元的に行い、
 * このDTOに totalAcceptedQty として含めて返す(一覧・詳細どちらも、この値を
 * そのまま表示するだけにする)。
 *
 * 併せて、「この発注に、過去1件でも保留が発生したことがあるか」を示す
 * hasHoldHistory も含める。発注一覧に「保留対応あり」のようなバッジを
 * 表示するためのトレーサビリティ強化に使う。
 */
public class MaterialOrderSummary {

    private Long orderId;
    private Long materialId;
    private Long supplierId;
    private BigDecimal orderQty;
    private String allowedOrigins;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private MaterialOrder.Status status;
    private BigDecimal totalAcceptedQty; // 通常の合格分 + 結局受け入れ分の合計
    private boolean hasHoldHistory;      // この発注に、過去1件でも保留が発生したことがあるか
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MaterialOrderSummary() {
    }

    public static MaterialOrderSummary from(MaterialOrder order, BigDecimal totalAcceptedQty, boolean hasHoldHistory) {
        MaterialOrderSummary summary = new MaterialOrderSummary();
        summary.orderId = order.getOrderId();
        summary.materialId = order.getMaterialId();
        summary.supplierId = order.getSupplierId();
        summary.orderQty = order.getOrderQty();
        summary.allowedOrigins = order.getAllowedOrigins();
        summary.orderDate = order.getOrderDate();
        summary.expectedDate = order.getExpectedDate();
        summary.status = order.getStatus();
        summary.totalAcceptedQty = totalAcceptedQty;
        summary.hasHoldHistory = hasHoldHistory;
        summary.createdAt = order.getCreatedAt();
        summary.updatedAt = order.getUpdatedAt();
        return summary;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public BigDecimal getOrderQty() {
        return orderQty;
    }

    public String getAllowedOrigins() {
        return allowedOrigins;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public LocalDate getExpectedDate() {
        return expectedDate;
    }

    public MaterialOrder.Status getStatus() {
        return status;
    }

    public BigDecimal getTotalAcceptedQty() {
        return totalAcceptedQty;
    }

    public boolean isHasHoldHistory() {
        return hasHoldHistory;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
