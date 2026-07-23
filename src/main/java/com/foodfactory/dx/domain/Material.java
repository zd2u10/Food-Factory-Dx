package com.foodfactory.dx.domain;

import java.time.LocalDateTime;

/**
 * 材料マスタに対応するJavaオブジェクト。
 * JPAの時と違い @Entity 等のアノテーションは一切付けない、ただのデータの入れ物(POJO)。
 */
public class Material {

    public enum Category {
        RAW,      // 原料(産地・賞味期限に紐づくロット番号が仕入先から発行される)
        ADDITIVE  // 添加物(賞味期限のみでロット番号が発行される。産地は管理基準外)
    }

    public enum BaseUnit {
        WEIGHT,  // 重量管理(g単位で内部保存)
        VOLUME   // 体積管理(ml単位で内部保存)
    }

    private Long materialId;         // 主キー。自動採番されるため新規作成時はnullでよい
    private String name;             // 材料名(例: 米粉、玄米粉、添加物A)
    private Category category;       // RAW(原料) or ADDITIVE(添加物)
    private BaseUnit baseUnit;       // WEIGHT(重量) or VOLUME(体積)。数量の単位系を決める
    private boolean mainMaterial;    // ベーカーズパーセント計算の基準になる主原料かどうか
    private LocalDateTime createdAt; // 登録日時(DB側で自動設定。読み取り専用)
    private LocalDateTime updatedAt; // 更新日時(DB側で自動設定。読み取り専用)

    public Material() {
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

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
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
