-- customer.required_residual_ratio(割合)を required_residual_days(日数)に置き換える。
-- 割合から日数への自動換算はできない(換算には対象商品の賞味期限日数が必要なため)。
-- 既に登録済みのデータがある場合は、この列を削除する前に値を控えておき、
-- ALTER後に手動で日数を入力し直すこと。
USE food_factory_dx;

ALTER TABLE customer
  DROP COLUMN required_residual_ratio,
  ADD COLUMN required_residual_days INT NULL
    COMMENT '出荷時に必要な残存期限の日数。指定なしはNULL'
    AFTER customer_type;
