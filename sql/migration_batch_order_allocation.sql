USE food_factory_dx;

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
    FOREIGN KEY (batch_id) REFERENCES manufacturing_batch (batch_id),
  CONSTRAINT fk_boa_order
    FOREIGN KEY (order_id) REFERENCES customer_order (order_id)
  -- 既存環境向けマイグレーションのため、customer_orderは既に存在している前提で、
  -- 外部キー制約もこの場でまとめて付けられる
  -- (setup_all.sql側は、実行順序の都合でphase4/phase5に分割していたが、
  --  こちらは既存DBへの追加なので、分割の必要がない)。
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
