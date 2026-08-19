package com.foodfactory.dx.domain;

import java.time.LocalDateTime;

/**
 * 仕入先マスタ。
 * 以前はmaterial_order/material_arrivalのsupplierIdを自由入力の文字列で管理していたが、
 * 「仕入先A」「仕入れ先A」のような表記ゆれが実データで発生し、
 * トレーサビリティを損なう実害が出たため、正式にマスタ化した。
 */
public class Supplier {

    private Long supplierId;         // 主キー
    private String name;             // 仕入先名(必須)
    private String address;          // 住所(任意)
    private String phoneNumber;      // 電話番号(任意)
    private boolean active = true;   // 有効/廃版フラグ。倒産・取引停止等でも過去の記録は残すため論理削除にする
    private LocalDateTime createdAt; // 登録日時(DB側で自動設定)
    private LocalDateTime updatedAt; // 更新日時(DB側で自動設定)

    public Supplier() {
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
