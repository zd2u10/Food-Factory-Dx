package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** バッチごとの材料使用・廃棄記録。 */
public class BatchMaterialUsage {

    public enum UsageType {
        CONSUMPTION, // 正常消費(レシピ通りに使われた分)
        DISPOSAL     // 廃棄(製造中のミス等による廃棄分)
    }

    private Long usageId;                  // 主キー
    private Long batchId;                  // どのバッチでの使用/廃棄か
    private Long materialLotId;            // どの材料ロットを使ったか
    private BigDecimal suggestedQty;       // FEFO計算による理論値(プレースホルダー表示用)
    private BigDecimal usedQty;            // 作業員が実際に入力した実測値
    private UsageType usageType = UsageType.CONSUMPTION; // 正常消費か廃棄か
    private String comment;                // 廃棄の場合の理由等(任意)

    private LocalDateTime createdAt;       // 登録日時(DB側で自動設定)
    private LocalDateTime updatedAt;       // 更新日時(DB側で自動設定)

    public BatchMaterialUsage() {
    }

    public BatchMaterialUsage(Long batchId, Long materialLotId, BigDecimal suggestedQty,
                              BigDecimal usedQty, UsageType usageType, String comment) {
        this.batchId = batchId;
        this.materialLotId = materialLotId;
        this.suggestedQty = suggestedQty;
        this.usedQty = usedQty;
        this.usageType = usageType;
        this.comment = comment;
    }

    public Long getUsageId() {
        return usageId;
    }

    public void setUsageId(Long usageId) {
        this.usageId = usageId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Long getMaterialLotId() {
        return materialLotId;
    }

    public void setMaterialLotId(Long materialLotId) {
        this.materialLotId = materialLotId;
    }

    public BigDecimal getSuggestedQty() {
        return suggestedQty;
    }

    public void setSuggestedQty(BigDecimal suggestedQty) {
        this.suggestedQty = suggestedQty;
    }

    public BigDecimal getUsedQty() {
        return usedQty;
    }

    public void setUsedQty(BigDecimal usedQty) {
        this.usedQty = usedQty;
    }

    public UsageType getUsageType() {
        return usageType;
    }

    public void setUsageType(UsageType usageType) {
        this.usageType = usageType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
