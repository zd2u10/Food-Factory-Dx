package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * レシピ明細(商品×材料)のJavaオブジェクト。
 * allowedOrigins は DB上ではカンマ区切りの1本の文字列(VARCHAR)として保存されている。
 */
public class RecipeItem {

    private Long recipeItemId;       // 主キー
    private Long itemId;             // どの商品のレシピか(items.itemIdを参照)
    private Long materialId;         // どの材料を使うか(material.materialIdを参照)
    private BigDecimal useQty;       // 使用量。バッチ1回あたりの固定量として扱う(個数に比例させない)
    private String allowedOrigins;   // 使用可能な産地をカンマ区切りで保持した生の文字列(例: "愛知,三重")
    private boolean mainMaterial;    // この商品における主原料か(ベーカーズパーセント計算の基準)
    private boolean liquid;          // 加水率計算に合算すべき液体材料か
    private LocalDateTime createdAt; // 登録日時(DB側で自動設定)
    private LocalDateTime updatedAt; // 更新日時(DB側で自動設定)

    public RecipeItem() {
    }

    public Long getRecipeItemId() {
        return recipeItemId;
    }

    public void setRecipeItemId(Long recipeItemId) {
        this.recipeItemId = recipeItemId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public BigDecimal getUseQty() {
        return useQty;
    }

    public void setUseQty(BigDecimal useQty) {
        this.useQty = useQty;
    }

    public String getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedOriginList() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public void setAllowedOriginList(List<String> origins) {
        this.allowedOrigins = String.join(",", origins);
    }

    public boolean isMainMaterial() {
        return mainMaterial;
    }

    public void setMainMaterial(boolean mainMaterial) {
        this.mainMaterial = mainMaterial;
    }

    public boolean isLiquid() {
        return liquid;
    }

    public void setLiquid(boolean liquid) {
        this.liquid = liquid;
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
