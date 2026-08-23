USE food_factory_dx;

ALTER TABLE manufacturing_batch
  ADD COLUMN actual_hydration_qty DECIMAL(10, 2) NULL
    COMMENT '実際に加えた水の実測量(ml)。トレーサビリティ記録用。
      「加水合計」(画面表示)は、この値と液体添加物の実測値合計を
      合算したもの(水そのものは材料マスタ・在庫の対象外のため、
      専用列として持たせている)'
    AFTER cancel_comment;
