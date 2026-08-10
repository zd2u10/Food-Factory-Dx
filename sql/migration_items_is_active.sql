USE food_factory_dx;

ALTER TABLE items
  ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE
  COMMENT '有効/廃版フラグ。物理削除はせず、廃版になったらFALSEにする(論理削除)'
  AFTER shelf_life_days;
