-- =====================================================
-- 仕入先マスタ テーブル定義
-- 対象DB: MySQL 8.0 CE
-- 対象テーブル: supplier(新規)、material_order・material_arrival(ALTER)
--
-- 【経緯】material_order.supplier_id・material_arrival.supplier_id は、
-- 当初は自由入力の文字列(VARCHAR)として設計していたが、
-- 「仕入先A」「仕入れ先A」のような表記ゆれが実データで発生し、
-- トレーサビリティ(この材料はどの仕入先から来たか)を損なう実害が出たため、
-- 正式にマスタ化することにした。
-- =====================================================

USE food_factory_dx;

-- -----------------------------------------------------
-- 仕入先マスタ
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS supplier (
  supplier_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  address       VARCHAR(255) NULL,
  phone_number  VARCHAR(20) NULL,
  is_active     BOOLEAN NOT NULL DEFAULT TRUE
    COMMENT '有効/廃版フラグ。倒産・取引停止等でも過去の記録は残すため、物理削除はせず論理削除にする',
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- material_order: supplier_id(文字列)を廃止し、
-- supplier.supplier_idを参照する新しい列(supplier_ref_id)を追加する。
-- 列名を変えているのは、移行が完了するまで新旧両方のデータを見比べられるようにするため。
-- 移行完了後、古いsupplier_id列は別途削除する想定(このファイルでは削除しない)。
-- -----------------------------------------------------
ALTER TABLE material_order
  ADD COLUMN supplier_ref_id BIGINT NULL AFTER supplier_id,
  ADD CONSTRAINT fk_mo_supplier
    FOREIGN KEY (supplier_ref_id) REFERENCES supplier (supplier_id);

ALTER TABLE material_arrival
  ADD COLUMN supplier_ref_id BIGINT NULL AFTER supplier_id,
  ADD CONSTRAINT fk_ma_supplier
    FOREIGN KEY (supplier_ref_id) REFERENCES supplier (supplier_id);
