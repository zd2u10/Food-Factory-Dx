-- =====================================================
-- フェーズ5: 注文管理・出荷 テーブル定義
-- 対象DB: MySQL 8.0 CE
-- 対象テーブル: customer, carrier, customer_order, order_line, shipment, shipment_line
-- 前提: phase0, phase1, phase2, phase3 のDDLが実行済みであること
-- =====================================================

USE food_factory_dx;

-- -----------------------------------------------------
-- 取引先マスタ
-- required_residual_days: 出荷時に必要な賞味期限の残存日数(例: 66日以上残っている必要がある)。
-- 割合(%)ではなく日数で持たせている理由: 現場が実際に判断する基準は
-- 「あと何日残っているか」であり、割合は商品ごとに賞味期限日数が違うと
-- 都度換算が必要になり直感的でないため(要件定義書 8.9節を参照)。
CREATE TABLE customer (
  customer_id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  name                    VARCHAR(100) NOT NULL,
  customer_type           ENUM('B2B', 'B2C') NOT NULL,
  required_residual_days  INT NULL COMMENT '出荷時に必要な残存期限の日数。指定なしはNULL',
  created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 配送会社マスタ
-- -----------------------------------------------------
CREATE TABLE carrier (
  carrier_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
  name         VARCHAR(100) NOT NULL,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 受注ヘッダー
-- -----------------------------------------------------
CREATE TABLE customer_order (
  order_id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_id             BIGINT NOT NULL,
  order_date              DATE NOT NULL,
  desired_delivery_date   DATE NULL,
  status                  ENUM('NEW', 'CONFIRMED', 'PARTIALLY_SHIPPED', 'COMPLETED', 'CANCELLED')
                           NOT NULL DEFAULT 'NEW',
  external_order_no       VARCHAR(100) NULL COMMENT '先方の注文システム上の番号(あれば)',
  created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_co_customer
    FOREIGN KEY (customer_id) REFERENCES customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 受注明細
-- amountはservice層で unit_price × qty として計算し保存する(冗長だが、
-- 後で単価が変わっても過去の受注時点の金額を保持できるようにするため)。
-- -----------------------------------------------------
CREATE TABLE order_line (
  line_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id     BIGINT NOT NULL,
  item_id      BIGINT NOT NULL,
  qty          DECIMAL(10, 2) NOT NULL COMMENT '注文数量(商品の個数)',
  unit_price   DECIMAL(10, 2) NULL COMMENT '単価(任意。記録のみ、請求書機能はスコープ外)',
  amount       DECIMAL(12, 2) NULL COMMENT '金額(unit_price × qty)',
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_ol_order
    FOREIGN KEY (order_id) REFERENCES customer_order (order_id),
  CONSTRAINT fk_ol_item
    FOREIGN KEY (item_id) REFERENCES items (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 出荷ヘッダー(配送1回分)
-- -----------------------------------------------------
CREATE TABLE shipment (
  shipment_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  carrier_id        BIGINT NOT NULL,
  shipped_date      DATE NOT NULL,
  destination       VARCHAR(255) NULL COMMENT '配送先住所等(自由記述)',
  temperature_zone  ENUM('FROZEN', 'AMBIENT') NOT NULL,
  created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_sh_carrier
    FOREIGN KEY (carrier_id) REFERENCES carrier (carrier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 出荷明細
-- 「1つの受注明細が複数バッチにまたがる」「1回の出荷が複数バッチ・複数受注にまたがる」
-- の両方に対応するため、order_lineとmanufacturing_batchを仲介する多対多の中間テーブルとする。
-- -----------------------------------------------------
CREATE TABLE shipment_line (
  line_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
  shipment_id    BIGINT NOT NULL,
  order_line_id  BIGINT NOT NULL,
  batch_id       BIGINT NOT NULL COMMENT '出荷元の製造バッチ(=商品ロット)',
  shipped_qty    DECIMAL(10, 2) NOT NULL,
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_sl_shipment
    FOREIGN KEY (shipment_id) REFERENCES shipment (shipment_id),
  CONSTRAINT fk_sl_order_line
    FOREIGN KEY (order_line_id) REFERENCES order_line (line_id),
  CONSTRAINT fk_sl_batch
    FOREIGN KEY (batch_id) REFERENCES manufacturing_batch (batch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- batch_order_allocation に order_id の外部キー制約を追加する
-- (ここでcustomer_orderが作成済みのため参照可能になる)。
-- -----------------------------------------------------
ALTER TABLE batch_order_allocation
  ADD CONSTRAINT fk_boa_order
  FOREIGN KEY (order_id) REFERENCES customer_order (order_id);
