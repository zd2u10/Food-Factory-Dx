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
