package com.foodfactory.dx.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 取引先マスタ。 */
public class Customer {

    public enum CustomerType {
        B2B,  // 法人取引(主に残存期限ルールが適用される)
        B2C   // 個人向け
    }

    private Long customerId;                    // 主キー
    private String name;                        // 取引先名
    private CustomerType customerType;          // B2B or B2C
    private BigDecimal requiredResidualRatio;   // 出荷時に必要な賞味期限の残存割合(0〜1)。指定なしはnull(主にB2C)
    private LocalDateTime createdAt;            // 登録日時(DB側で自動設定)
    private LocalDateTime updatedAt;            // 更新日時(DB側で自動設定)

    public Customer() {
    }

    public Customer(String name, CustomerType customerType, BigDecimal requiredResidualRatio) {
        this.name = name;
        this.customerType = customerType;
        this.requiredResidualRatio = requiredResidualRatio;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public void setCustomerType(CustomerType customerType) {
        this.customerType = customerType;
    }

    public BigDecimal getRequiredResidualRatio() {
        return requiredResidualRatio;
    }

    public void setRequiredResidualRatio(BigDecimal requiredResidualRatio) {
        this.requiredResidualRatio = requiredResidualRatio;
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
