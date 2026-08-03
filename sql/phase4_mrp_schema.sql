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
-- manufacturing_batch への変更(既存環境向け)
-- -----------------------------------------------------

-- ステータスにCANCELLEDを追加(製造開始前の取り消しに対応)
ALTER TABLE manufacturing_batch
  MODIFY COLUMN status ENUM('DRAFT', 'PLAN', 'MANUFACTURING', 'COMPLETED', 'REJECTED', 'CANCELLED')
  NOT NULL DEFAULT 'DRAFT';

-- CANCELLEDになった場合の理由コメント列を追加
-- (既に列が存在する場合はエラーになるため、実行前に念のため確認することを推奨)
ALTER TABLE manufacturing_batch
  ADD COLUMN cancel_comment VARCHAR(255) NULL COMMENT 'CANCELLEDになった場合の理由(製造開始前の取り消し)'
  AFTER reject_comment;

-- mrp_runテーブルが今できたので、mrp_run_idに外部キー制約を追加する
ALTER TABLE manufacturing_batch
  ADD CONSTRAINT fk_mb_mrp_run
  FOREIGN KEY (mrp_run_id) REFERENCES mrp_run (run_id);
