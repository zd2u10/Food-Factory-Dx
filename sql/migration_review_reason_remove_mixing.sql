USE food_factory_dx;

-- review_reason に MIXING_MISTAKE が既に入っているデータがあれば、
-- 先にSTORAGE_ISSUEなど適切な値に手動で更新してから、このALTERを実行すること
-- (ENUMの選択肢から除外すると、該当データはエラーになる可能性がある)。
-- 例: UPDATE material_lot SET review_reason = 'STORAGE_ISSUE' WHERE review_reason = 'MIXING_MISTAKE';

ALTER TABLE material_lot
  MODIFY COLUMN review_reason ENUM('STORAGE_ISSUE', 'CONTAMINATION', 'OTHER') NULL
    COMMENT '要確認になった理由(保管ミス/異物混入/その他)。
      「配合ミス」は同一ロットのまま完結する破棄する操作の理由のため、
      ここには含めない';
