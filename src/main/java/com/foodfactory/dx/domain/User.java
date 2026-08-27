package com.foodfactory.dx.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

/**
 * ユーザーアカウント。業務理解レベルによる3階層の権限モデル
 * (1=一般作業員, 2=主任・リーダー, 3=管理者)を持つ(要件定義書8.27節を参照)。
 *
 * パスワードは平文では一切保持しない。passwordHashには、必ずBCryptで
 * ハッシュ化した文字列だけが入る。
 */
public class User {

    private Long userId;
    private String username;
    private String passwordHash;
    private int accessLevel; // 1=一般作業員, 2=主任・リーダー, 3=管理者
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * パスワードハッシュは、絶対にAPIレスポンス(JSON)に含めてはならないため、
     * @JsonIgnore を付ける(ログイン成功時やGET /api/auth/meで、Userオブジェクトを
     * そのまま返しているため、これが無いとハッシュ値がフロントに丸見えになってしまう)。
     */
    @JsonIgnore
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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
