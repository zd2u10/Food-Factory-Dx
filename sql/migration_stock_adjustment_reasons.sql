USE food_factory_dx;

ALTER TABLE stock_adjustment
  ADD COLUMN usage_discard_reason ENUM('MIXING_MISTAKE', 'MATERIAL_DEFECT', 'CONTAMINATION', 'OTHER') NULL
    COMMENT '製造実行画面(FEFO)の「破棄する」操作による調整の場合の理由
      (配合ミス/材料の不備を確認/異物混入/その他)。
      在庫調整画面からの調整、その他の調整(結局受け入れ等)ではNULL'
    AFTER adjustment_date,
  ADD COLUMN stock_review_reason ENUM('EXPIRED', 'STORAGE_ISSUE', 'CONTAMINATION', 'OTHER') NULL
    COMMENT '在庫調整画面(検査結果登録)からの調整の場合の理由
      (期限切れ/保管ミス/異物混入/その他)。
      製造実行画面からの破棄、その他の調整ではNULL'
    AFTER usage_discard_reason;
