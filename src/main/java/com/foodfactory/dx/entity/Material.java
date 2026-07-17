package com.foodfactory.dx.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 材料マスタ
 * 原料(RAW)・添加物(ADDITIVE)を一元管理する。
 */
@Entity
@Table(name = "material")
public class Material {

    public enum Category {
        RAW,
        ADDITIVE
    }

    public enum BaseUnit {
        WEIGHT,
        VOLUME
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private Long materialId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "base_unit", nullable = false)
    private BaseUnit baseUnit;

    @Column(name = "is_main_material", nullable = false)
    private boolean mainMaterial;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected Material() {
        // JPA用
    }

    public Material(String name, Category category, BaseUnit baseUnit, boolean mainMaterial) {
        this.name = name;
        this.category = category;
        this.baseUnit = baseUnit;
        this.mainMaterial = mainMaterial;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BaseUnit getBaseUnit() {
        return baseUnit;
    }

    public void setBaseUnit(BaseUnit baseUnit) {
        this.baseUnit = baseUnit;
    }

    public boolean isMainMaterial() {
        return mainMaterial;
    }

    public void setMainMaterial(boolean mainMaterial) {
        this.mainMaterial = mainMaterial;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
