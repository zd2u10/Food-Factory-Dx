package com.foodfactory.dx.dto;

import java.math.BigDecimal;

/**
 * FEFO自動選定の結果、「どの材料ロットから、いくつ使うべきか」を1件分表すクラス。
 *
 * domainクラスとの違い: このクラスはDBのどのテーブルにも対応しない。
 * あくまで「Service層が計算した結果を、Controller経由で画面に伝えるためだけの入れ物」であり、
 * このオブジェクト自体がDBに保存されることはない。
 * このような「保存されない、やり取り専用のオブジェクト」をDTO(Data Transfer Object)と呼ぶ。
 */
public class FefoAllocationLine {

    private Long materialId;
    private Long materialLotId;
    private String supplierLotNo;
    private String origin;
    private BigDecimal allocatedQty; // このロットから使うべき量(理論値=suggestedQtyになる)
    private Long originHoldId; // このロットが「結局受け入れ」(ACCEPTED_LATE)によって生成された
                                // 場合、元になったhold_resolution.hold_id。通常のロットはnull
                                // (製造実行画面で、保留対応を経た材料であることを示すバッジ表示に使う)

    public FefoAllocationLine() {
    }

    public FefoAllocationLine(Long materialId, Long materialLotId, String supplierLotNo,
                               String origin, BigDecimal allocatedQty, Long originHoldId) {
        this.materialId = materialId;
        this.materialLotId = materialLotId;
        this.supplierLotNo = supplierLotNo;
        this.origin = origin;
        this.allocatedQty = allocatedQty;
        this.originHoldId = originHoldId;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public Long getMaterialLotId() {
        return materialLotId;
    }

    public void setMaterialLotId(Long materialLotId) {
        this.materialLotId = materialLotId;
    }

    public String getSupplierLotNo() {
        return supplierLotNo;
    }

    public void setSupplierLotNo(String supplierLotNo) {
        this.supplierLotNo = supplierLotNo;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public BigDecimal getAllocatedQty() {
        return allocatedQty;
    }

    public void setAllocatedQty(BigDecimal allocatedQty) {
        this.allocatedQty = allocatedQty;
    }

    public Long getOriginHoldId() {
        return originHoldId;
    }

    public void setOriginHoldId(Long originHoldId) {
        this.originHoldId = originHoldId;
    }
}
