-- 過去データの仕入先名寄せ・紐付け
--
-- 【名寄せルール】送り仮名の違いは無視して統一する。
--   「仕入先A」「仕入れ先A」→ どちらも同一の仕入先として「仕入先A」に統一する
--   (末尾のアルファベットが異なる場合は別の仕入先として扱う。今回はA1件のみ確認)
--
-- 実行前提: sql/migration_supplier_master.sql が実行済みで、
--   supplierテーブル、material_order.supplier_ref_id、material_arrival.supplier_ref_id が
--   既に存在していること。

USE food_factory_dx;

-- 1. 仕入先マスタに「仕入先A」を1件登録する
INSERT INTO supplier (name, is_active)
VALUES ('仕入先A', TRUE);

SET SQL_SAFE_UPDATES = 0;

-- 2. 今登録した仕入先のsupplier_idを、既存の発注・入荷データに紐付ける。
--    「仕入先A」「仕入れ先A」のどちらの表記であっても、同じsupplier_idを設定する。
UPDATE material_order
SET supplier_ref_id = (SELECT supplier_id FROM supplier WHERE name = '仕入先A')
WHERE supplier_id IN ('仕入先A', '仕入れ先A');

UPDATE material_arrival
SET supplier_ref_id = (SELECT supplier_id FROM supplier WHERE name = '仕入先A')
WHERE supplier_id IN ('仕入先A', '仕入れ先A');

-- 3. 紐付け漏れが無いか確認する(結果が0件になっているべき)
SELECT * FROM material_order WHERE supplier_ref_id IS NULL;
SELECT * FROM material_arrival WHERE supplier_ref_id IS NULL;
