package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * レシピ明細(商品×材料)のJavaオブジェクト。
 * allowedOrigins は DB上ではカンマ区切りの1本の文字列(VARCHAR)として保存されている。
 * Java側で扱いやすいように、リスト形式に変換するメソッドを用意している。
 */
public class RecipeItem {

    private Long recipeItemId;

    // JPAの時は item / material をオブジェクトとして持たせていたが、
    // MyBatisでは外部キーのIDだけを持たせるのが基本形(前述のMaterialPackageSpecと同じ考え方)。
    private Long itemId;
    private Long materialId;

    private BigDecimal useQty;

    // DBに保存されている生の文字列(例: "愛知,三重")。
    // フィールド名を allowedOrigins のままにすると紛らわしいので、
    // 「DBに入っている生の値そのもの」であることが分かるよう明示的な名前にしている。
    private String allowedOrigins;

    private boolean mainMaterial;

    private boolean liquid;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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
     *
     * 処理の内訳:
     *   1. split(",")            : カンマの位置で文字列を分割し、配列にする
     *   2. Arrays.stream(...)    : 配列をStream(処理をつなげて書けるようにする仕組み)に変換
     *   3. .map(String::trim)    : 各要素の前後の余分な空白を取り除く(" 三重" のような入力ミスに強くする)
     *   4. .filter(...)          : 空文字("")になった要素を除外する(末尾に余分なカンマがあった場合の対策)
     *   5. .collect(...)         : Streamの処理結果を、最終的に List<String> の形に集約する
     */
    public List<String> getAllowedOriginList() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * ["愛知", "三重"] のようなリストを "愛知,三重" のカンマ区切り文字列に変換して保持する。
     * String.join(",", origins) は、リストの各要素をカンマで繋いで1本の文字列にするメソッド。
     */
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
