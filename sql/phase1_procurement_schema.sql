-- =====================================================
-- フェーズ1: 発注・入荷・材料ロット テーブル定義
-- 対象DB: MySQL 8.0 CE
-- 対象テーブル: material_order, material_arrival, material_arrival_line, material_lot
-- 前提: phase0_master_schema.sql が実行済みであること(material テーブルを参照するため)
-- =====================================================

USE food_factory_dx;

-- -----------------------------------------------------
-- 仕入先マスタ
-- 当初material_order/material_arrivalのsupplierIdは自由入力の文字列で管理していたが、
-- 「仕入先A」「仕入れ先A」のような表記ゆれが実データで発生し、トレーサビリティを
-- 損なう実害が出たため、正式にマスタ化した(8.14節を参照)。
-- 論理削除(is_active)を持たせる: 倒産・取引停止等でも過去の発注・入荷記録は
-- そのまま追跡できるよう、物理削除ではなく論理削除にしている。
-- -----------------------------------------------------
CREATE TABLE supplier (
  supplier_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  address       VARCHAR(255) NULL,
  phone_number  VARCHAR(20) NULL,
  is_active     BOOLEAN NOT NULL DEFAULT TRUE
    COMMENT '有効/廃版フラグ。倒産・取引停止等でも過去の記録は残すため、物理削除はせず論理削除にする',
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 発注記録(発注ヘッダー)
-- 1件の発注が、複数回に分けて納品される(分納)ことがあるため、
-- 発注そのものと、実際の入荷は別テーブルに分離している。
-- -----------------------------------------------------
CREATE TABLE material_order (
  order_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  material_id    BIGINT NOT NULL,
  supplier_ref_id BIGINT NOT NULL COMMENT '仕入先(supplier.supplier_idを参照)',
  order_qty      DECIMAL(10, 2) NOT NULL COMMENT '発注数量(g または ml)',
  allowed_origins VARCHAR(255) NULL
    COMMENT 'この発注で許可する産地をカンマ区切りで保持(例: "愛知,三重")。
      recipe_item.allowed_originsと同じ形式。任意項目で、
      未指定の場合は産地を問わない発注として扱う',
  order_date     DATE NOT NULL,
  expected_date  DATE NULL COMMENT '納品予定日',
  status         ENUM('NOT_ARRIVED', 'PARTIALLY_ARRIVED', 'FULLY_ARRIVED')
                 NOT NULL DEFAULT 'NOT_ARRIVED'
                 COMMENT '未入荷/一部入荷/入荷完了。入荷明細の合格数量を集計して判定する',
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_mo_material
    FOREIGN KEY (material_id) REFERENCES material (material_id),
  CONSTRAINT fk_mo_supplier
    FOREIGN KEY (supplier_ref_id) REFERENCES supplier (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 入荷ヘッダー(伝票1枚 = 1回の配送イベント)
-- 1回の配送で複数の異なる材料・複数の異なる発注がまとめて届くことがあるため、
-- material_id/order_idはこのヘッダーではなく、明細(material_arrival_line)側に持たせる。
-- ヘッダーは「いつ・どの仕入先から届いたか」という配送イベントの情報だけを持つ。
-- -----------------------------------------------------
CREATE TABLE material_arrival (
  arrival_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
  supplier_ref_id BIGINT NOT NULL COMMENT '仕入先(supplier.supplier_idを参照)',
  arrival_date   DATE NOT NULL,
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_ma_supplier
    FOREIGN KEY (supplier_ref_id) REFERENCES supplier (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 入荷明細(ロット単位)
-- 1回の伝票(material_arrival)の中に、産地・賞味期限・仕入先ロット番号が異なる
-- 複数のロットが混在するケースや、そもそも異なる材料・異なる発注が混在するケースがあるため、
-- material_id/order_idはこの明細単位で持つ。
-- 検品(破損・期限切れ・異物混入)はこの明細単位で行い、
-- 同じ明細の中でも合格数量(accepted_qty)と保留数量(held_qty)を分けて記録する。
-- -----------------------------------------------------
CREATE TABLE material_arrival_line (
  line_id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
  arrival_id                 BIGINT NOT NULL,
  material_id                BIGINT NOT NULL COMMENT 'この明細で届いた材料。1回の配送内で複数材料が混在してもよいよう明細側に持つ',
  order_id                   BIGINT NULL COMMENT '対応する発注(緊急入荷等、発注に紐づかない場合はNULL)。1回の配送内で複数発注が混在してもよいよう明細側に持つ',
  supplier_lot_no            VARCHAR(100) NOT NULL COMMENT '仕入先が発行したロット番号',
  origin                     VARCHAR(100) NOT NULL COMMENT '産地(原料の場合)。添加物は仕入先区分などを入れる',
  expiry_date                DATE NOT NULL COMMENT '賞味期限(FEFO判定の基準になる)',
  package_count              INT NOT NULL COMMENT '入荷した箱数/袋数',
  package_weight_snapshot    DECIMAL(10, 2) NOT NULL COMMENT '入荷時点での1箱あたり目安重量のスナップショット',
  arrived_qty                DECIMAL(10, 2) NOT NULL COMMENT 'package_count × package_weight_snapshot で計算した総量',
  accepted_qty               DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '検品合格数量(在庫に反映される分)',
  held_qty                   DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '検品保留数量(在庫には反映しない)',
  check_damage               BOOLEAN NOT NULL DEFAULT TRUE COMMENT '検品項目:破損がないか(TRUE=問題なし)',
  check_expiry               BOOLEAN NOT NULL DEFAULT TRUE COMMENT '検品項目:期限切れでないか',
  check_contamination        BOOLEAN NOT NULL DEFAULT TRUE COMMENT '検品項目:異物混入の兆候がないか',
  exchange_source_line_id    BIGINT NULL COMMENT '交換品の場合、元の保留明細を参照する(自己参照)',
  created_at                 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at                 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_mal_arrival
    FOREIGN KEY (arrival_id) REFERENCES material_arrival (arrival_id),
  CONSTRAINT fk_mal_material
    FOREIGN KEY (material_id) REFERENCES material (material_id),
  CONSTRAINT fk_mal_order
    FOREIGN KEY (order_id) REFERENCES material_order (order_id),
  CONSTRAINT fk_mal_exchange_source
    FOREIGN KEY (exchange_source_line_id) REFERENCES material_arrival_line (line_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 材料ロット(在庫の実体)
-- 入荷明細のうち「合格した分(accepted_qty)」だけが、このテーブルに1件のロットとして生成される。
-- 1つの入荷明細から1つのロットが生成される(1対1)。
-- remaining_qty は、製造での消費・廃棄のたびに減っていく「現在の残量」。
-- -----------------------------------------------------
CREATE TABLE material_lot (
  lot_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  material_id      BIGINT NOT NULL,
  arrival_line_id  BIGINT NOT NULL COMMENT '生成元となった入荷明細(産地・賞味期限等のトレース元)',
  supplier_lot_no  VARCHAR(100) NOT NULL COMMENT '仕入先が発行したロット番号(arrival_lineからコピー、表示の利便性のため)',
  origin           VARCHAR(100) NOT NULL,
  expiry_date      DATE NOT NULL,
  remaining_qty    DECIMAL(10, 2) NOT NULL COMMENT '残量。消費/廃棄のたびにService層で減算する',
  needs_review     BOOLEAN NOT NULL DEFAULT FALSE
                   COMMENT '製造実行画面で「別ロットに切り替える」操作が行われた場合にtrueになる。
                     trueのロットは、以降のFEFO自動選定(previewFefoAllocation)の対象から
                     動的に除外される。人が検査結果を登録する(生存量入力/全量破棄)まで、
                     残量(remaining_qty)自体はそのまま変更しない
                     (「本当に全部ダメか、一部は使えるか」の判断を保留する設計。8.21節を参照)',
  review_reason    ENUM('STORAGE_ISSUE', 'CONTAMINATION', 'OTHER') NULL
                   COMMENT '要確認になった理由(配合ミス/保管ミス/異物混入/その他)',
  review_comment   VARCHAR(255) NULL COMMENT '理由がOTHERの場合の自由記述、または補足コメント',
  created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_ml_material
    FOREIGN KEY (material_id) REFERENCES material (material_id),
  CONSTRAINT fk_ml_arrival_line
    FOREIGN KEY (arrival_line_id) REFERENCES material_arrival_line (line_id)
  -- 【重要】origin_hold_id列(結局受け入れ対応のロット分離用)は、ここでは定義しない。
  -- hold_resolutionテーブルがphase3で作られるため、参照先が存在しない状態になってしまう。
  -- そのため、phase3_hold_adjustment_schema.sqlの末尾で、hold_resolution作成後に
  -- ALTER TABLEで追加する(実行順序: phase0→1→2→3の順を守ること)。
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
