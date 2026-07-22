package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 材料の梱包仕様(産地ごとの1箱/袋あたりの目安数量)。
 * JPAの時と同様、数量はBigDecimalで扱う(重量・体積は誤差の蓄積を避けるため)。
 */
public class MaterialPackageSpec {

    private Long specId;

    // JPAの時は @ManyToOne で Material 型そのものを持たせていたが、
    // MyBatisではオブジェクト参照ではなく、外部キーの値(materialId)をそのまま持たせるのが基本形。
    // (関連先のオブジェクトが必要な場合は、別途取得するSQLを呼び出して自分で組み立てる)
    private Long materialId;

    private String origin;

    private BigDecimal packageWeight;

    private String packageUnitLabel;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
