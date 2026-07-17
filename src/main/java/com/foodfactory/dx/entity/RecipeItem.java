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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * レシピ明細(商品×材料)
 * allowedOrigins はカンマ区切りの文字列としてDBに保存し、
 * getAllowedOriginList() でリスト形式に変換して扱う。
 */
@Entity
@Table(
        name = "recipe_item",
        uniqueConstraints = @UniqueConstraint(columnNames = {"item_id", "material_id"})
)
public class RecipeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_item_id")
    private Long recipeItemId;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "use_qty", nullable = false, precision = 10, scale = 2)
    private BigDecimal useQty;

    @Column(name = "allowed_origins", nullable = false, length = 255)
    private String allowedOrigins;

    @Column(name = "is_main_material", nullable = false)
    private boolean mainMaterial;

    @Column(name = "is_liquid", nullable = false)
    private boolean liquid;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    protected RecipeItem() {
        // JPA用
    }

    public RecipeItem(Item item, Material material, BigDecimal useQty,
                       List<String> allowedOriginList, boolean mainMaterial, boolean liquid) {
        this.item = item;
        this.material = material;
        this.useQty = useQty;
        setAllowedOriginList(allowedOriginList);
        this.mainMaterial = mainMaterial;
        this.liquid = liquid;
    }

    public Long getRecipeItemId() {
        return recipeItemId;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
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

    /** カンマ区切りの許可産地文字列をリストに変換して取得する */
    public List<String> getAllowedOriginList() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /** 許可産地リストをカンマ区切り文字列に変換して保存する */
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
