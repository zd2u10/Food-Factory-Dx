USE food_factory_dx;

ALTER TABLE material_lot
  ADD COLUMN needs_review BOOLEAN NOT NULL DEFAULT FALSE
    COMMENT '製造実行画面で「別ロットに切り替える」操作が行われた場合にtrueになる。
      trueのロットは、以降のFEFO自動選定の対象から動的に除外される。
      人が検査結果を登録するまで、残量自体はそのまま変更しない'
    AFTER remaining_qty,
  ADD COLUMN review_reason ENUM('STORAGE_ISSUE', 'CONTAMINATION', 'OTHER') NULL
    COMMENT '要確認になった理由(配合ミス/保管ミス/異物混入/その他)'
    AFTER needs_review,
  ADD COLUMN review_comment VARCHAR(255) NULL
    COMMENT '理由がOTHERの場合の自由記述、または補足コメント'
    AFTER review_reason;
