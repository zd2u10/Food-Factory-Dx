-- =====================================================
-- 食品工場DXシステム 完全セットアップSQL(2026-08-20 最新版)
--
-- 【使い方】このファイル1本を、まっさらなMySQLに対して上から実行するだけで、
-- 現在のバックエンドコードが期待する最新のスキーマが作られる。
-- 既存のfood_factory_dxデータベースがある場合は、実行前にDROPされる
-- (テストデータは全て失われるので注意)。
--
-- 中身は、以下のファイルを実行順に単純結合したもの:
--   phase0_master_schema.sql
--   phase1_procurement_schema.sql (supplierマスタを統合済み)
--   phase2_manufacturing_schema.sql
--   phase3_hold_adjustment_schema.sql (material_lot.origin_hold_id追加を含む)
--   phase4_mrp_schema.sql
--   phase5_order_shipment_schema.sql (customer.required_residual_daysを含む)
--
-- 個別のmigration_*.sqlファイルは、この統合作業により全て不要になった
-- (使い終えた過去の記録として、削除はせずそのまま残してある)。
-- =====================================================

DROP DATABASE IF EXISTS food_factory_dx;

-- ===== phase0_master_schema.sql =====
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
  can_mix             BOOLEAN NOT NULL DEFAULT FALSE
    COMMENT '複数の産地が混在する可能性はあるか。trueの産地同士は、発注時に
      重量・単位が一致するものが自動でグループ化され、まとめて1つの選択肢として扱われる
      (例: 愛知・三重が両方trueなら「愛知or三重」という1つのチェックボックスになる)。
      falseの産地は常に単独で表示される(例: 山梨産限定のレシピがある場合など)',
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

-- ===== phase1_procurement_schema.sql =====
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

-- ===== phase2_manufacturing_schema.sql =====
-- =====================================================
-- フェーズ2: 製造管理 テーブル定義
-- 対象DB: MySQL 8.0 CE
-- 対象テーブル: manufacturing_batch, batch_material_usage
-- 前提: phase0, phase1 のDDLが実行済みであること
-- =====================================================

USE food_factory_dx;

-- -----------------------------------------------------
-- 製造バッチ(=製造ロット)
-- 1回の製造実行を1件のレコードとして管理する。
-- mrp_run_id は将来のフェーズ4(MRP自動化)で使う項目のため、
-- 現時点では mrp_run テーブル自体が存在せず、外部キー制約は付けていない。
-- -----------------------------------------------------
CREATE TABLE manufacturing_batch (
  batch_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
  item_id         BIGINT NOT NULL,
  mrp_run_id      BIGINT NULL COMMENT 'フェーズ4で使用予定。現時点ではmrp_runテーブル未実装のためFK制約なし',
  batch_date      DATE NOT NULL,
  batch_seq       INT NOT NULL COMMENT 'その日・その商品の何バッチ目か(1から始まる連番)',
  status          ENUM('DRAFT', 'PLAN', 'MANUFACTURING', 'COMPLETED', 'REJECTED', 'CANCELLED')
                  NOT NULL DEFAULT 'DRAFT',
  origin_type     ENUM('MRP_AUTO', 'MANUAL') NOT NULL DEFAULT 'MANUAL'
                  COMMENT 'フェーズ2時点ではMRP自動化が未実装のため、常にMANUALになる',
  created_by      VARCHAR(100) NULL COMMENT '手動追加時の担当者(任意)',
  planned_qty     DECIMAL(10, 2) NOT NULL COMMENT '通常は items.standard_batch_qty と同値',
  produced_qty    DECIMAL(10, 2) NULL COMMENT '完了時に確定する製造数(合格+不良の合計)',
  accepted_qty    DECIMAL(10, 2) NULL COMMENT '完了時に確定する合格数(商品在庫に計上される数)',
  remaining_qty   DECIMAL(10, 2) NULL COMMENT '出荷等で減っていく残量。完了時にaccepted_qtyと同値で初期化される(フェーズ5で使用)',
  exceeded_plan   BOOLEAN NOT NULL DEFAULT FALSE COMMENT '完了時、produced_qty(合格+不良)がplanned_qtyを超えていた場合にtrue',
  loss_qty        DECIMAL(10, 2) NULL COMMENT '完了時に確定する軽微な不良数',
  loss_comment    VARCHAR(255) NULL,
  reject_comment  VARCHAR(255) NULL COMMENT 'REJECTEDになった場合の理由',
  cancel_comment  VARCHAR(255) NULL COMMENT 'CANCELLEDになった場合の理由(製造開始前の取り消し)',
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_mb_item
    FOREIGN KEY (item_id) REFERENCES items (item_id),
  CONSTRAINT uq_mb_item_date_seq UNIQUE (item_id, batch_date, batch_seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- バッチごとの材料使用記録
-- suggested_qty(FEFO計算の理論値)と used_qty(作業員の実測入力値)を分けて記録する。
-- usage_type で「正常消費」と「製造中の廃棄」を区別する。
-- -----------------------------------------------------
CREATE TABLE batch_material_usage (
  usage_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  batch_id         BIGINT NOT NULL,
  material_lot_id  BIGINT NOT NULL,
  suggested_qty    DECIMAL(10, 2) NOT NULL COMMENT 'FEFO計算による理論値(プレースホルダー)',
  used_qty         DECIMAL(10, 2) NOT NULL COMMENT '作業員が実際に入力した実測値',
  usage_type       ENUM('CONSUMPTION', 'DISPOSAL') NOT NULL DEFAULT 'CONSUMPTION',
  comment          VARCHAR(255) NULL COMMENT '廃棄の場合の理由等',
  created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_bmu_batch
    FOREIGN KEY (batch_id) REFERENCES manufacturing_batch (batch_id),
  CONSTRAINT fk_bmu_lot
    FOREIGN KEY (material_lot_id) REFERENCES material_lot (lot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===== phase3_hold_adjustment_schema.sql =====
-- =====================================================
-- フェーズ3: 保留対応・手動在庫調整 テーブル定義
-- 対象DB: MySQL 8.0 CE
-- 対象テーブル: hold_resolution, stock_adjustment
-- 前提: phase0, phase1, phase2 のDDLが実行済みであること
-- =====================================================

USE food_factory_dx;

-- -----------------------------------------------------
-- 保留対応記録
-- 検品で一部保留(held_qty > 0)が発生した入荷明細に対して、
-- 「返品」「交換」「結局受け入れる」のいずれで決着したかを記録する。
-- 保留が発生した時点でこのテーブルに1件、status=ON_HOLDで自動的に作られる想定
-- (resolution_typeは対応方針が決まるまではNULLのまま)。
-- -----------------------------------------------------
CREATE TABLE hold_resolution (
  hold_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  line_id            BIGINT NOT NULL COMMENT '保留が発生した元の入荷明細',
  held_qty_snapshot  DECIMAL(10, 2) NOT NULL
                     COMMENT '保留発生時点の保留数量のスナップショット(元明細のheld_qtyが後で0に書き換わっても追跡できる)',
  resolution_type    ENUM('RETURNED', 'EXCHANGED', 'ACCEPTED_LATE') NULL
                     COMMENT '対応方針。返品/交換/結局受け入れ。未確定の間はNULL',
  resolved_line_id   BIGINT NULL COMMENT '交換品として新規登録された入荷明細(EXCHANGEDの場合のみ)',
  status             ENUM('ON_HOLD', 'RESOLVED') NOT NULL DEFAULT 'ON_HOLD',
  comment            VARCHAR(255) NULL,
  created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_hr_line
    FOREIGN KEY (line_id) REFERENCES material_arrival_line (line_id),
  CONSTRAINT fk_hr_resolved_line
    FOREIGN KEY (resolved_line_id) REFERENCES material_arrival_line (line_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- 在庫の手動調整記録
-- material_lot.remaining_qty を直接書き換えるのではなく、必ずこのテーブルを経由して
-- 変更履歴(誰が・いつ・なぜ変えたか)を残す運用にする(監査要件対応)。
-- -----------------------------------------------------
CREATE TABLE stock_adjustment (
  adjustment_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
  lot_id           BIGINT NOT NULL,
  before_qty       DECIMAL(10, 2) NOT NULL COMMENT '調整前の数量',
  after_qty        DECIMAL(10, 2) NOT NULL COMMENT '調整後の数量',
  adjustment_date  DATE NOT NULL,
  comment          VARCHAR(255) NOT NULL COMMENT '調整理由(必須)',
  created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_sa_lot
    FOREIGN KEY (lot_id) REFERENCES material_lot (lot_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- material_lot に origin_hold_id を追加する(ここでhold_resolutionが作成済みのため参照可能になる)。
-- このロットが「結局受け入れ」(ACCEPTED_LATE)によって生成された場合、
-- 元になったhold_resolution.hold_idを記録する。通常の入荷で作られたロットはNULLのまま。
-- 「普通に合格した分」と「一度保留を経て受け入れた分」を、ロット単位で区別できるようにする。
-- -----------------------------------------------------
ALTER TABLE material_lot
  ADD COLUMN origin_hold_id BIGINT NULL AFTER arrival_line_id,
  ADD CONSTRAINT fk_ml_origin_hold
    FOREIGN KEY (origin_hold_id) REFERENCES hold_resolution (hold_id);

-- ===== phase4_mrp_schema.sql =====
-- =====================================================
-- フェーズ4: MRP自動化 テーブル定義
-- 対象DB: MySQL 8.0 CE
-- 対象テーブル: mrp_run(新規)、manufacturing_batch(ALTER)
-- 前提: phase0〜phase3, phase5 のDDLが実行済みであること
--
-- 【重要】このファイルはALTER文を使っており、既にmanufacturing_batchに
-- データが入っている環境(=これまでのテスト環境)にもそのまま適用できる想定。
-- 新規に環境を作り直す場合は、phase2_manufacturing_schema.sql自体を
-- 最新版(CANCELLED/cancel_comment込み)で実行すれば、このファイルの
-- ALTER部分は不要(CREATE TABLE mrp_runの部分だけ実行すればよい)。
-- =====================================================

USE food_factory_dx;

-- -----------------------------------------------------
-- MRP実行履歴
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS mrp_run (
  run_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
  run_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  triggered_by  ENUM('AUTO', 'MANUAL', 'EVENT') NOT NULL
                COMMENT 'AUTO=1日1回の定期実行, MANUAL=人による手動実行, EVENT=CANCELLED/REJECTED発生時の即時再計算',
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- manufacturing_batch への変更
-- 【注記】statusのCANCELLED対応・cancel_comment列は、既にphase2の
-- CREATE TABLE manufacturing_batch 本体に統合済みのため、ここでは
-- mrp_run作成後にしか追加できない外部キー制約の追加だけを行う。
-- -----------------------------------------------------

-- mrp_runテーブルが今できたので、mrp_run_idに外部キー制約を追加する
ALTER TABLE manufacturing_batch
  ADD CONSTRAINT fk_mb_mrp_run
  FOREIGN KEY (mrp_run_id) REFERENCES mrp_run (run_id);

-- ===== phase5_order_shipment_schema.sql =====
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

