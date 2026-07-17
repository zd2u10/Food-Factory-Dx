package com.foodfactory.dx.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 材料の梱包仕様マスタ
 * 産地(仕入先)ごとに「1箱/袋あたりの目安数量」を保持する。
 * 入荷登録時に箱数から総量(g/ml)を自動計算するための補助データ。
 */
@Entity
@Table(
        name = "material_package_spec",
        uniqueConstraints = @UniqueConstraint(columnNames = {"material_id", "origin"})
)
public class MaterialPackageSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spec_id")
    private Long specId;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "origin", nullable = false, length = 100)
    private String origin;

    @Column(name = "package_weight", nullable = false, precision = 10, scale = 2)
    private BigDecimal packageWeight;

    @Column(name = "package_unit_label", nullable = false, length = 20)
    private String packageUnitLabel;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected MaterialPackageSpec() {
        // JPA用
    }

    public MaterialPackageSpec(Material material, String origin, BigDecimal packageWeight, String packageUnitLabel) {
        this.material = material;
        this.origin = origin;
        this.packageWeight = packageWeight;
        this.packageUnitLabel = packageUnitLabel;
    }

    public Long getSpecId() {
        return specId;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
