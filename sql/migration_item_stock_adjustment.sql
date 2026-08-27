USE food_factory_dx;

-- -----------------------------------------------------
-- 商品在庫調整履歴
--
-- COMPLETED(検品完了・在庫化済み)の商品ロットに対する、
-- バックヤード担当者による手動調整(保管・取り扱い不良、期限切れ等による廃棄)を記録する。
-- MANUFACTURING中の重大な異常による破棄は、製造現場の既存機能で引き続き扱う。
-- -----------------------------------------------------
CREATE TABLE item_stock_adjustment (
  adjustment_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
  batch_id         BIGINT NOT NULL,
  before_qty       DECIMAL(10, 2) NOT NULL COMMENT '調整前の残数量(remaining_qty)',
  after_qty        DECIMAL(10, 2) NOT NULL COMMENT '調整後の残数量',
  adjustment_date  DATE NOT NULL,
  adjustment_reason ENUM('STORAGE_HANDLING_ISSUE', 'EXPIRED', 'OTHER') NOT NULL
                   COMMENT '保管・取り扱い不良/期限切れ/その他',
  comment          VARCHAR(255) NOT NULL COMMENT '調整理由の詳細(必須)',
  created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_isa_batch
    FOREIGN KEY (batch_id) REFERENCES manufacturing_batch (batch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
