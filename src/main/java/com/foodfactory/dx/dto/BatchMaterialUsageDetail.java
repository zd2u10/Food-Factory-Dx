package com.foodfactory.dx.dto;

import com.foodfactory.dx.domain.BatchMaterialUsage;
import java.math.BigDecimal;

/**
 * 製造実績一覧の「使用材料の内訳を見る」で表示する、1件分の使用実績の詳細情報。
 * batch_material_usage だけでは材料名・仕入先ロット番号までは分からないため、
 * material_lot・material をまとめてJOINした結果を1つのDTOとして返す
 * (要件定義書8.26節を参照)。
 */
public class BatchMaterialUsageDetail {

    private Long usageId;
    private Long materialId;
    private String materialName;
    private String supplierLotNo;
    private BigDecimal suggestedQty;
    private BigDecimal usedQty;
    private BatchMaterialUsage.UsageType usageType;
    private String comment;
    private Long originHoldId; // 「結局受け入れ」経由のロットの場合、hold_resolution.hold_id。通常はnull

    public BatchMaterialUsageDetail() {
    }

    public BatchMaterialUsageDetail(Long usageId, Long materialId, String materialName, String supplierLotNo,
                                     BigDecimal suggestedQty, BigDecimal usedQty,
                                     BatchMaterialUsage.UsageType usageType, String comment, Long originHoldId) {
        this.usageId = usageId;
        this.materialId = materialId;
        this.materialName = materialName;
        this.supplierLotNo = supplierLotNo;
        this.suggestedQty = suggestedQty;
        this.usedQty = usedQty;
        this.usageType = usageType;
        this.comment = comment;
        this.originHoldId = originHoldId;
    }

    public Long getUsageId() {
        return usageId;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public String getSupplierLotNo() {
        return supplierLotNo;
    }

    public BigDecimal getSuggestedQty() {
        return suggestedQty;
    }

    public BigDecimal getUsedQty() {
        return usedQty;
    }

    public BatchMaterialUsage.UsageType getUsageType() {
        return usageType;
    }

    public String getComment() {
        return comment;
    }

    public Long getOriginHoldId() {
        return originHoldId;
    }
}
