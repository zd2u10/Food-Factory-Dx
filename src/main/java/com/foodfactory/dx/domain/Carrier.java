package com.foodfactory.dx.domain;

import java.time.LocalDateTime;

/** 配送会社マスタ。 */
public class Carrier {

    private Long carrierId;          // 主キー
    private String name;             // 配送会社名
    private LocalDateTime createdAt; // 登録日時(DB側で自動設定)
    private LocalDateTime updatedAt; // 更新日時(DB側で自動設定)

    public Carrier() {
    }

    public Carrier(String name) {
        this.name = name;
    }

    public Long getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(Long carrierId) {
        this.carrierId = carrierId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
