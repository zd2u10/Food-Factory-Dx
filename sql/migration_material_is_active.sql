-- =====================================================
-- material テーブルへの論理削除(is_active)列の追加
-- 既にデータが入っている環境に対して、データを消さずに列だけ追加する。
-- =====================================================

USE food_factory_dx;

ALTER TABLE material
  ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE
  COMMENT '有効/廃版フラグ。物理削除はせず、廃版になったらFALSEにする(論理削除)'
  AFTER is_main_material;
