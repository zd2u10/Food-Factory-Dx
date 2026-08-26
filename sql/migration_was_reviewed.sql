USE food_factory_dx;

ALTER TABLE material_lot
  ADD COLUMN was_reviewed BOOLEAN NOT NULL DEFAULT FALSE
    COMMENT '一度でも「要確認」→検査結果登録(生存量として復帰)を経た場合、trueになる。
      needs_reviewが解除された後も、この履歴は消さずに残す。
      製造実績一覧で「検査後の材料を使用」バッジを表示するために使う'
    AFTER review_comment;
