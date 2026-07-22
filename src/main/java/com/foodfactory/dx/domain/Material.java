package com.foodfactory.dx.domain;

import java.time.LocalDateTime;

/**
 * 材料マスタに対応するJavaオブジェクト。
 *
 * JPAの時と違い、このクラスには @Entity や @Table のようなアノテーションは一切付けない。
 * MyBatisでは「このクラスがどのテーブルと対応するか」をこのクラス自身は知らず、
 * 代わりに mapper配下のXMLファイルの中で
 * 「SELECT結果のどの列を、このクラスのどのフィールドに詰めるか」を明示的に書く。
 * つまりこのクラスは、ただの「データを持ち運ぶための入れ物(POJO: Plain Old Java Object)」でしかない。
 */
public class Material {

    // --- カテゴリ・単位はDB上ではただの文字列(VARCHAR/ENUM)として保存されるため、
    //     Java側では列挙型(enum)として定義しておくと、
    //     "RAW"や"raw"のようなタイプミスをコンパイル時に防げる。 ---
    public enum Category {
        RAW,
        ADDITIVE
    }

    public enum BaseUnit {
        WEIGHT,
        VOLUME
    }

    // material_id は自動採番(AUTO_INCREMENT)されるため、
    // 新規作成時はnullのままでよく、DB登録後にMyBatisが採番されたIDを詰め直してくれる。
    private Long materialId;

    private String name;

    private Category category;

    private BaseUnit baseUnit;

    // Java の boolean はプリミティブ型(必ずtrue/falseが入る、nullになれない)。
    // is_main_material のように「はい/いいえ」の値はこれで十分表現できる。
    private boolean mainMaterial;

    // created_at/updated_at はDB側で自動設定される値なので、
    // Java側では「読み取り専用」として扱う(setterはあえて用意しない)。
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // MyBatisは「引数なしコンストラクタ + setter」でオブジェクトを組み立てるのが基本の動き方。
    // (SELECT結果の1行ごとに、まずこの空のコンストラクタでインスタンスを作り、
    //  列の値を1つずつsetterで詰めていく)
    public Material() {
    }

    // 新規登録時に使う用のコンストラクタ。呼び出し側が書きやすいように用意している。
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
