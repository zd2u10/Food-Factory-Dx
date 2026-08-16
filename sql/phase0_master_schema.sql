-- =====================================================
-- フェーズ0: 基盤マスタ テーブル定義
-- 対象DB: MySQL 8.0 CE
-- 対象テーブル: material, material_package_spec, items, recipe_item
-- =====================================================

CREATE DATABASE IF NOT EXISTS food_factory_dx
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE food_factory_dx;

-- -----------------------------------------------------
-- 材料マスタ
-- -----------------------------------------------------
CREATE TABLE material (
  material_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  name              VARCHAR(100)  NOT NULL COMMENT '材料名(例:米粉、玄米粉)',
  category          ENUM('RAW', 'ADDITIVE') NOT NULL COMMENT 'RAW=原料 / ADDITIVE=添加物',
  base_unit         ENUM('WEIGHT', 'VOLUME') NOT NULL COMMENT 'WEIGHT=重量(g) / VOLUME=体積(ml)',
  is_main_material  BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'ベーカーズパーセント計算の基準材料か',
  is_active         BOOLEAN NOT NULL DEFAULT TRUE COMMENT '有効/廃版フラグ。物理削除はせず、廃版になったらFALSEにする(論理削除)',
  created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 材料の梱包仕様マスタ(産地ごとの1箱/袋あたりの目安重量)
-- -----------------------------------------------------
CREATE TABLE material_package_spec (
  spec_id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  material_id         BIGINT NOT NULL,
  origin              VARCHAR(100) NOT NULL COMMENT '産地・仕入先区分(例:愛知、新潟)',
  package_weight      DECIMAL(10, 2) NOT NULL COMMENT '1箱/袋あたりの目安数量(g または ml)',
  package_unit_label  VARCHAR(20) NOT NULL COMMENT '表示用単位名(箱、袋、缶など)',
  created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_mps_material
    FOREIGN KEY (material_id) REFERENCES material (material_id),
  CONSTRAINT uq_mps_material_origin UNIQUE (material_id, origin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 商品マスタ
-- -----------------------------------------------------
CREATE TABLE items (
  item_id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  name                VARCHAR(100) NOT NULL COMMENT '商品名',
  safety_stock_qty    DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '適正在庫',
  target_stock_qty    DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '目標在庫(将来拡張用)',
  standard_batch_qty  DECIMAL(10, 2) NOT NULL COMMENT '1バッチあたりの標準製造数(季節変動込み平均値)',
  shelf_life_days     INT NOT NULL DEFAULT 90 COMMENT '賞味期限日数',
  hydration_ratio_min DECIMAL(5, 2) NULL COMMENT '加水率の下限(%)。職人が試作の上で確立した、季節変動込みの基準値',
  hydration_ratio_max DECIMAL(5, 2) NULL COMMENT '加水率の上限(%)',
  hydration_qty_min   DECIMAL(10, 2) NULL COMMENT '加水量(溶液合計)の下限(ml)。主原料の使用量×加水率で算出した参考値',
  hydration_qty_max   DECIMAL(10, 2) NULL COMMENT '加水量(溶液合計)の上限(ml)',
  is_active           BOOLEAN NOT NULL DEFAULT TRUE COMMENT '有効/廃版フラグ。物理削除はせず、廃版になったらFALSEにする(論理削除)',
  created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- レシピ明細(商品×材料)
-- -----------------------------------------------------
CREATE TABLE recipe_item (
  recipe_item_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
  item_id            BIGINT NOT NULL,
  material_id        BIGINT NOT NULL,
  use_qty            DECIMAL(10, 2) NOT NULL COMMENT '使用量(g または ml、material.base_unitに従う)',
  allowed_origins    VARCHAR(255) NOT NULL COMMENT 'カンマ区切りで使用可能な産地を列挙(例:愛知,三重)',
  is_main_material   BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'この商品における主原料か',
  is_liquid          BOOLEAN NOT NULL DEFAULT FALSE COMMENT '加水率計算に合算する液体material_id か',
  created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_ri_item
    FOREIGN KEY (item_id) REFERENCES items (item_id),
  CONSTRAINT fk_ri_material
    FOREIGN KEY (material_id) REFERENCES material (material_id),
  CONSTRAINT uq_ri_item_material UNIQUE (item_id, material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
