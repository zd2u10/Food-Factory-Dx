USE food_factory_dx;

-- 既存のUNIQUE制約を撤廃する(制約名は環境によって異なる場合があるため、
-- 事前に SHOW CREATE TABLE manufacturing_batch; で実際の制約名を確認してから実行すること)。
ALTER TABLE manufacturing_batch DROP INDEX uq_mb_item_date_seq;

-- batch_date / batch_seq をNULL許容に変更する。
-- まだどの日にも配置されていないDraft(未配置プール)を表現するために必要。
ALTER TABLE manufacturing_batch
  MODIFY COLUMN batch_date DATE NULL
    COMMENT 'まだどの日にも配置されていないDraft(未配置プール)はNULL。
      デイリー画面で特定の日に配置した時点で、その日付がセットされる',
  MODIFY COLUMN batch_seq INT NULL
    COMMENT 'その日・その商品の何バッチ目か(1から始まる連番)。
      batch_dateと同様、未配置の間はNULLのままで、配置された時点で採番する';

-- 撤廃したUNIQUE制約の代わりに、検索性能維持のための通常インデックスを付けておく。
ALTER TABLE manufacturing_batch
  ADD INDEX idx_mb_item_date_seq (item_id, batch_date, batch_seq);
