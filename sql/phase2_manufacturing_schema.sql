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
  status          ENUM('DRAFT', 'PLAN', 'MANUFACTURING', 'COMPLETED', 'REJECTED')
                  NOT NULL DEFAULT 'DRAFT',
  origin_type     ENUM('MRP_AUTO', 'MANUAL') NOT NULL DEFAULT 'MANUAL'
                  COMMENT 'フェーズ2時点ではMRP自動化が未実装のため、常にMANUALになる',
  created_by      VARCHAR(100) NULL COMMENT '手動追加時の担当者(任意)',
  planned_qty     DECIMAL(10, 2) NOT NULL COMMENT '通常は items.standard_batch_qty と同値',
  produced_qty    DECIMAL(10, 2) NULL COMMENT '完了時に確定する製造数(合格+不良の合計)',
  accepted_qty    DECIMAL(10, 2) NULL COMMENT '完了時に確定する合格数(商品在庫に計上される数)',
  remaining_qty   DECIMAL(10, 2) NULL COMMENT '出荷等で減っていく残量。完了時にaccepted_qtyと同値で初期化される(フェーズ5で使用)',
  loss_qty        DECIMAL(10, 2) NULL COMMENT '完了時に確定する軽微な不良数',
  loss_comment    VARCHAR(255) NULL,
  reject_comment  VARCHAR(255) NULL COMMENT 'REJECTEDになった場合の理由',
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
