package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 1つの製造バッチのうち、どの受注に、どれだけの数量が起因しているかを表す。
 *
 * 【設計意図】MRPが1回の実行で複数商品・複数バッチをまとめて生成する際、
 * 「安全在庫由来」と「受注由来」が同じバッチの中に混在しうる
 * (例: 198個のバッチのうち、受注A30個+受注B40個+安全在庫分128個)。
 * このクラスは、その按分のうち「受注由来」の部分だけを明細として記録する。
 * 「按分されていない残り」(バッチのplannedQty - このバッチの按分合計)が、
 * 安全在庫由来の部分を表す(暗黙的に、レコードを作らないことで表現する)。
 *
 * 受注がキャンセルされても、このレコード自体は削除しない(履歴として残す)。
 * 判定ロジック(そのバッチ自体をキャンセルすべきか)は、Service層で、
 * 「安全在庫由来の部分が残っているか」を見て行う。
 */
public class BatchOrderAllocation {

    private Long allocationId;
    private Long batchId;
    private Long orderId;
    private BigDecimal allocatedQty;
    private LocalDateTime createdAt;

    public BatchOrderAllocation() {
    }

    public BatchOrderAllocation(Long batchId, Long orderId, BigDecimal allocatedQty) {
        this.batchId = batchId;
        this.orderId = orderId;
        this.allocatedQty = allocatedQty;
    }

    public Long getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(Long allocationId) {
        this.allocationId = allocationId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAllocatedQty() {
        return allocatedQty;
    }

    public void setAllocatedQty(BigDecimal allocatedQty) {
        this.allocatedQty = allocatedQty;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
