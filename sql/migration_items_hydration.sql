USE food_factory_dx;

ALTER TABLE items
  ADD COLUMN hydration_ratio_min DECIMAL(5, 2) NULL
    COMMENT '加水率の下限(%)。職人が試作の上で確立した、季節変動込みの基準値'
    AFTER shelf_life_days,
  ADD COLUMN hydration_ratio_max DECIMAL(5, 2) NULL
    COMMENT '加水率の上限(%)'
    AFTER hydration_ratio_min,
  ADD COLUMN hydration_qty_min DECIMAL(10, 2) NULL
    COMMENT '加水量(溶液合計)の下限(ml)。主原料の使用量×加水率で算出した参考値'
    AFTER hydration_ratio_max,
  ADD COLUMN hydration_qty_max DECIMAL(10, 2) NULL
    COMMENT '加水量(溶液合計)の上限(ml)'
    AFTER hydration_qty_min;
