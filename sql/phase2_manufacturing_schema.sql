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
  batch_date      DATE NULL
                  COMMENT 'まだどの日にも配置されていないDraft(未配置プール)はNULL。
                    デイリー画面で特定の日に配置した時点で、その日付がセットされる
                    (要件定義書8.19節を参照)',
  batch_seq       INT NULL
                  COMMENT 'その日・その商品の何バッチ目か(1から始まる連番)。
                    batch_dateと同様、未配置の間はNULLのままで、配置された時点で採番する',
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
  actual_hydration_qty DECIMAL(10, 2) NULL
                  COMMENT '実際に加えた水の実測量(ml)。トレーサビリティ記録用。
                    「加水合計」(画面表示)は、この値と液体添加物の実測値合計を
                    合算したもの(水そのものは材料マスタ・在庫の対象外のため、
                    専用列として持たせている。要件定義書8.20節を参照)',
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_mb_item
    FOREIGN KEY (item_id) REFERENCES items (item_id),
  -- 【重要】以前はUNIQUE制約だったが、batch_date/batch_seqがNULL許容になったことに伴い撤廃した。
  -- MySQLのUNIQUE制約はNULL同士を「重複ではない」とみなすため、複数の未配置Draft
  -- (batch_date=NULL, batch_seq=NULL)が同じitem_idで共存することは制約上問題ないが、
  -- 「配置済みのもの同士(NULLでない値)が重複しないこと」は、アプリケーション側
  -- (Service層でfindMaxBatchSeqを使って採番する際の排他制御)で保証する。
  KEY idx_mb_item_date_seq (item_id, batch_date, batch_seq)
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
