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

    public RecipeItem(Long itemId, Long materialId, BigDecimal useQty,
                       List<String> allowedOriginList, boolean mainMaterial, boolean liquid) {
        this.itemId = itemId;
        this.materialId = materialId;
        this.useQty = useQty;
        setAllowedOriginList(allowedOriginList);
        this.mainMaterial = mainMaterial;
        this.liquid = liquid;
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

    /**
     * "愛知,三重" のようなカンマ区切り文字列を ["愛知", "三重"] のリストに変換する。
     *   1. split(",")         : カンマの位置で文字列を分割し配列にする
     *   2. Arrays.stream(...) : 配列をStreamに変換
     *   3. .map(String::trim) : 各要素の前後の余分な空白を取り除く
     *   4. .filter(...)       : 空文字を除外する(末尾に余分なカンマがあった場合の対策)
     *   5. .collect(...)      : 最終的にList<String>に集約する
     */
    public List<String> getAllowedOriginList() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /** ["愛知", "三重"] のようなリストを "愛知,三重" のカンマ区切り文字列に変換して保持する。 */
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
