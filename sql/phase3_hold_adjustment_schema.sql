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
  usage_discard_reason ENUM('MIXING_MISTAKE', 'MATERIAL_DEFECT', 'CONTAMINATION', 'OTHER') NULL
                   COMMENT '製造実行画面(FEFO)の「破棄する」操作による調整の場合の理由
                     (配合ミス/材料の不備を確認/異物混入/その他)。
                     在庫調整画面からの調整、その他の調整(結局受け入れ等)ではNULL。
                     stock_review_reasonとは互いに排他的(どちらか一方だけが値を持つ)',
  stock_review_reason  ENUM('EXPIRED', 'STORAGE_ISSUE', 'CONTAMINATION', 'OTHER') NULL
                   COMMENT '在庫調整画面(検査結果登録)からの調整の場合の理由
                     (期限切れ/保管ミス/異物混入/その他)。
                     製造実行画面からの破棄、その他の調整ではNULL。
                     usage_discard_reasonとは互いに排他的。
                     理由をFEFO画面用・在庫調整画面用の2列に分けているのは、
                     一方の画面で選ぶはずのない理由(期限切れのロットはFEFO候補に
                     出てこない、配合ミスは在庫調整では起こらない、等)が、
                     選択肢に混在しないようにするため(要件定義書8.22節を参照)',
  comment          VARCHAR(255) NOT NULL COMMENT '調整理由の詳細(必須)。
                     理由がOTHERの場合は、具体的な内容を必ず記入する',
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

-- -----------------------------------------------------
-- 商品在庫調整履歴
--
-- material_lot向けのstock_adjustmentと同じ考え方で、manufacturing_batch
-- (COMPLETED状態、商品ロットとして在庫化されたもの)に対する手動調整
-- (棚卸で見つかった保管・取り扱い不良、期限切れ等による廃棄)を記録する。
-- remaining_qtyを直接書き換えるのではなく、必ずこのテーブルを経由して
-- 変更履歴(調整前・調整後・理由)を残す運用にする(要件定義書8.25節を参照)。
--
-- 【対象範囲】このテーブルで扱うのは、COMPLETED(検品完了・在庫化済み)の
-- 商品ロットに対する、バックヤード(在庫保管)担当者による調整のみ。
-- MANUFACTURING中の重大な異常による破棄(REJECTED)は、製造現場の既存機能
-- (completeBatch画面の「重大な異常のため破棄する」)で引き続き扱う。
-- -----------------------------------------------------
CREATE TABLE item_stock_adjustment (
  adjustment_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
  batch_id         BIGINT NOT NULL,
  before_qty       DECIMAL(10, 2) NOT NULL COMMENT '調整前の残数量(remaining_qty)',
  after_qty        DECIMAL(10, 2) NOT NULL COMMENT '調整後の残数量',
  adjustment_date  DATE NOT NULL,
  adjustment_reason ENUM('STORAGE_HANDLING_ISSUE', 'EXPIRED', 'OTHER') NOT NULL
                   COMMENT '保管・取り扱い不良/期限切れ/その他。
                     保管ミスと破損は、原因分類としての実務的な意味が薄いため
                     1つに統合している(要件定義書8.25節を参照)',
  comment          VARCHAR(255) NOT NULL COMMENT '調整理由の詳細(必須)。
                     理由がOTHERの場合は、具体的な内容を必ず記入する',
  created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_isa_batch
    FOREIGN KEY (batch_id) REFERENCES manufacturing_batch (batch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
