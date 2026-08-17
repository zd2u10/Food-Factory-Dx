USE food_factory_dx;

ALTER TABLE material_package_spec
  ADD COLUMN can_mix BOOLEAN NOT NULL DEFAULT FALSE
  COMMENT '複数の産地が混在する可能性はあるか。trueの産地同士は、発注時に
    重量・単位が一致するものが自動でグループ化され、まとめて1つの選択肢として扱われる
    (例: 愛知・三重が両方trueなら「愛知or三重」という1つのチェックボックスになる)。
    falseの産地は常に単独で表示される(例: 山梨産限定のレシピがある場合など)'
  AFTER package_unit_label;
