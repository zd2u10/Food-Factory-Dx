USE food_factory_dx;

-- -----------------------------------------------------
-- ユーザーアカウント
--
-- 業務理解レベルによる3階層の権限モデル:
--   1 = 一般作業員 / 2 = 主任・リーダー / 3 = 管理者
-- パスワードは平文では一切保存せず、BCryptでハッシュ化した文字列のみを保存する。
-- -----------------------------------------------------
CREATE TABLE user (
  user_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
  username      VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL COMMENT 'BCryptでハッシュ化した文字列。平文は保存しない',
  access_level  TINYINT NOT NULL COMMENT '1=一般作業員, 2=主任・リーダー, 3=管理者',
  is_active     BOOLEAN NOT NULL DEFAULT TRUE COMMENT '退職者等の無効化用(削除ではなくフラグで管理)',
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初期管理者アカウント。パスワードは "admin123"(BCryptでハッシュ化済み)。
-- ログイン後、パスワード変更機能で、必ず変更すること。
INSERT INTO user (username, password_hash, access_level, is_active)
VALUES ('システム設計者', '$2b$12$.roD4fAbPX2PuAuF9byC3uobdA2Iv18LhrQm7bvujECbwM81Za9k2', 3, TRUE);
