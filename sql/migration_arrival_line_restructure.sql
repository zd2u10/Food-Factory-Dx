-- =====================================================
-- material_arrival / material_arrival_line の構造変更に伴う、
-- 関連テストデータのリセット手順
-- (material_id/order_idをヘッダーから明細へ移動したことによる構造変更のため)
-- =====================================================

USE food_factory_dx;

-- 外部キー制約を一時的に無効化してから、影響範囲のテーブルを削除する
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS stock_adjustment;
DROP TABLE IF EXISTS hold_resolution;
DROP TABLE IF EXISTS batch_material_usage;
DROP TABLE IF EXISTS material_lot;
DROP TABLE IF EXISTS material_arrival_line;
DROP TABLE IF EXISTS material_arrival;

SET FOREIGN_KEY_CHECKS = 1;

-- この後、以下の順で再実行してください
--   1. sql/phase1_procurement_schema.sql (材料入荷まわり、構造変更を反映した最新版)
--   2. sql/phase2_manufacturing_schema.sql (batch_material_usageのみ作り直される)
--   3. sql/phase3_hold_adjustment_schema.sql (hold_resolution, stock_adjustment新規作成)

-- manufacturing_batch(バッチ本体)はテーブル構造自体は変更していないため残しても良いが、
-- batch_material_usage/material_lotを削除したことで、
-- 既にMANUFACTURING/COMPLETEDまで進んでいたバッチの実行内容と整合しなくなる。
-- テスト用データのため、以下でPLAN状態まで巻き戻してから、
-- 発注→入荷→検品→FEFOプレビュー→実行→完了を最初からやり直すことを推奨する。
UPDATE manufacturing_batch
SET status = 'PLAN',
    produced_qty = NULL,
    accepted_qty = NULL,
    remaining_qty = NULL,
    exceeded_plan = FALSE,
    loss_qty = NULL,
    loss_comment = NULL
WHERE status IN ('MANUFACTURING', 'COMPLETED');
