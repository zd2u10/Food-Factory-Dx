-- 前回追加した use_qty_min/use_qty_max を取り消す(recipe_item側では不要と判断したため)
USE food_factory_dx;

ALTER TABLE recipe_item
  DROP COLUMN use_qty_min,
  DROP COLUMN use_qty_max;
