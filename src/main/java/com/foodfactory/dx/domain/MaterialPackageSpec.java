package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 材料の梱包仕様(産地ごとの1箱/袋あたりの目安数量)。
 */
public class MaterialPackageSpec {

    private Long specId;                     // 主キー
    private Long materialId;                 // どの材料の梱包仕様か(material.materialIdを参照)
    private String origin;                   // 産地・仕入先区分(例: 愛知、新潟)
    private BigDecimal packageWeight;         // 1箱/袋あたりの目安数量(重量or体積、material.baseUnitに従う)
    private String packageUnitLabel;         // 表示用の単位名(箱、袋、缶など)
    private LocalDateTime createdAt;         // 登録日時(DB側で自動設定)
    private LocalDateTime updatedAt;         // 更新日時(DB側で自動設定)

    public MaterialPackageSpec() {
    }

    public MaterialPackageSpec(Long materialId, String origin, BigDecimal packageWeight, String packageUnitLabel) {
        this.materialId = materialId;
        this.origin = origin;
        this.packageWeight = packageWeight;
        this.packageUnitLabel = packageUnitLabel;
    }

    public Long getSpecId() {
        return specId;
    }

    public void setSpecId(Long specId) {
        this.specId = specId;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public BigDecimal getPackageWeight() {
        return packageWeight;
    }

    public void setPackageWeight(BigDecimal packageWeight) {
        this.packageWeight = packageWeight;
    }

    public String getPackageUnitLabel() {
        return packageUnitLabel;
    }

    public void setPackageUnitLabel(String packageUnitLabel) {
        this.packageUnitLabel = packageUnitLabel;
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
