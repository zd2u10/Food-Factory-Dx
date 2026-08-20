USE food_factory_dx;

-- 以前はarrival_line_idにUNIQUE制約を付け、1明細=1ロットとしていたが、
-- 「結局受け入れ」(ACCEPTED_LATE)対応で、同じ明細から後になって
-- 追加のロットが生成されるケースが生まれたため、この制約を撤廃する。
-- 制約名は環境によって異なる場合があるため、まずSHOW CREATE TABLEで確認してから
-- 実際のインデックス名に置き換えて実行すること。
-- (多くの場合、以下のどちらかの名前になっているはず)
ALTER TABLE material_lot DROP INDEX uq_ml_arrival_line;

-- origin_hold_id: このロットが「結局受け入れ」によって生成された場合、
-- 元になったhold_resolution.hold_idを記録する列を追加する。
ALTER TABLE material_lot
  ADD COLUMN origin_hold_id BIGINT NULL
    COMMENT 'このロットが「結局受け入れ」(ACCEPTED_LATE)によって生成された場合、
      元になったhold_resolution.hold_idを記録する。通常の入荷で作られたロットはNULLのまま'
    AFTER arrival_line_id,
  ADD CONSTRAINT fk_ml_origin_hold
    FOREIGN KEY (origin_hold_id) REFERENCES hold_resolution (hold_id);
