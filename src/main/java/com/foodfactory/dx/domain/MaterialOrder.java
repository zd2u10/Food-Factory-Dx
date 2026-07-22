package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 発注記録(発注ヘッダー)。
 * 「材料を何g/ml発注したか」だけを表し、実際に届いたかどうかは material_arrival 側で管理する。
 */
public class MaterialOrder {

    /**
     * 発注の状態。
     * この値はService層が「紐づく入荷明細の合格数量の合計」を計算して自動的に更新する想定
     * (人がボタンで手動で変更するものではない)。
     */
    public enum Status {
        NOT_ARRIVED,        // 未入荷
        PARTIALLY_ARRIVED,  // 一部入荷
        FULLY_ARRIVED       // 入荷完了
    }

    private Long orderId;
    private Long materialId;
    private String supplierId;
    private BigDecimal orderQty;
    private LocalDate orderDate;
    private LocalDate expectedDate;

    // フィールド自体に初期値を持たせている点がポイント。
    //
    // 以前は「引数付きのコンストラクタの中でNOT_ARRIVEDを設定する」という書き方をしていたが、
    // Jackson(JSON→Javaオブジェクトの変換を行うライブラリ)がリクエストボディを変換する際は
    // 「引数なしのコンストラクタでまず空のオブジェクトを作り、その後setterで値を詰めていく」
    // という動き方をするため、引数付きのコンストラクタは一切呼ばれない。
    // その結果、JSONの中にstatusという項目が含まれていない場合、
    // statusフィールドは初期化されないまま(=null)残ってしまい、
    // NOT NULL制約のあるDB列に対してnullを送ろうとしてエラーになっていた。
    //
    // フィールド宣言の時点で初期値を持たせておけば、
    // 「引数なしコンストラクタ経由で作られる場合」を含め、常にこの初期値からスタートするため、
    // このような抜けが起きない。
    private Status status = Status.NOT_ARRIVED;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
