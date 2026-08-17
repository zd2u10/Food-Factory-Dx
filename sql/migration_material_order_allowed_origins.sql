USE food_factory_dx;

ALTER TABLE material_order
  ADD COLUMN allowed_origins VARCHAR(255) NULL
  COMMENT 'この発注で許可する産地をカンマ区切りで保持(例: "愛知,三重")。
    recipe_item.allowed_originsと同じ形式。任意項目で、
    未指定の場合は産地を問わない発注として扱う'
  AFTER order_qty;
