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
-- バッチ別受注按分
--
-- MRPが1回の実行で、複数商品・複数バッチをまとめて生成する際、
-- 「1つのバッチのうち、どの受注に、どれだけの数量が起因しているか」を記録する。
--
-- 【設計意図】受注がキャンセルされた際、「そのバッチをキャンセルすべきか」を
-- 正確に判定するために必要。バッチ全体を「受注由来 or 安全在庫由来」の
-- どちらか一方に分類するのではなく、1つのバッチの中に両方が混在しうる
-- (例: 198個のバッチのうち、受注A30個+受注B40個+安全在庫分128個)ため、
-- 按分を明細として記録する中間テーブルにしている。
--
-- 「按分されていない残り」(plannedQty - このバッチのallocated_qty合計)が、
-- 安全在庫由来の部分を表す(暗黙的に、レコードを作らないことで表現する)。
--
-- 受注キャンセル時のルール:
--   このテーブルの按分レコード自体は、履歴として残す(削除しない)。
--   ただし、「安全在庫由来の部分が残っている(0より大きい)場合、
--   そのバッチはキャンセルしない」というルールで、
--   紐づく全受注がキャンセルされても、安全在庫分が守られるようにする。
-- -----------------------------------------------------
CREATE TABLE batch_order_allocation (
  allocation_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
  batch_id       BIGINT NOT NULL,
  order_id       BIGINT NOT NULL,
  allocated_qty  DECIMAL(10, 2) NOT NULL COMMENT 'このバッチのうち、この受注に按分された数量',
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_boa_batch
    FOREIGN KEY (batch_id) REFERENCES manufacturing_batch (batch_id)
  -- 【重要】order_idへの外部キー制約(customer_order参照)は、ここでは付けない。
  -- customer_orderテーブルはphase5(受注・出荷)で作られるため、
  -- phase4の時点ではまだ存在しない(material_lot.origin_hold_idと同じパターン)。
  -- 制約の追加は、phase5_order_shipment_schema.sqlの末尾で行う
  -- (実行順序: phase0→1→2→3→4→5の順を守ること)。
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
