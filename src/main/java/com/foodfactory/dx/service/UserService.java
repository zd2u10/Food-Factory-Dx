package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.User;
import com.foodfactory.dx.mapper.UserMapper;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * ユーザーアカウントの管理(新規登録、一覧取得、無効化)に関するService。
 * 新規登録は、権限レベル3(管理者)専用の操作とする
 * (マスタの廃版・無効化と同じ重大さの操作のため。要件定義書8.27節を参照)。
 */
@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> listUsers() {
        return userMapper.findAll();
    }

    /**
     * 新規ユーザーを登録する。パスワードは、この場でBCryptによりハッシュ化してから保存する
     * (呼び出し元(Controller)から、平文のパスワードをそのまま受け取る)。
     */
    public User createUser(String username, String rawPassword, int accessLevel) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("ユーザー名を入力してください。");
        }
        if (userMapper.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("このユーザー名は、既に使用されています。");
        }
        if (!StringUtils.hasText(rawPassword) || rawPassword.length() < 8) {
            throw new IllegalArgumentException("パスワードは8文字以上で入力してください。");
        }
        if (accessLevel < 1 || accessLevel > 3) {
            throw new IllegalArgumentException("権限レベルは1〜3の範囲で指定してください。");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setAccessLevel(accessLevel);
        user.setActive(true);
        userMapper.insert(user);
        return user;
    }

    public void setActive(Long userId, boolean isActive) {
        userMapper.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("指定されたユーザーが見つかりません: userId=" + userId));
        userMapper.updateActive(userId, isActive);
    }

    /**
     * 管理者(レベル3)が、他ユーザーのパスワードを直接、新しいものに上書きする。
     * 登録時の入力ミスや、パスワードを忘れた場合の訂正に使う想定。
     * 本人の「現在のパスワード」の確認は行わない(既に管理者権限を持っている
     * ことをControllerで確認済みのため。本人自身によるパスワード変更
     * (AuthService.changePassword)とは異なる操作であることに注意)。
     */
    public void resetPassword(Long userId, String newPassword) {
        userMapper.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("指定されたユーザーが見つかりません: userId=" + userId));
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 8) {
            throw new IllegalArgumentException("パスワードは8文字以上で入力してください。");
        }
        userMapper.updatePasswordHash(userId, passwordEncoder.encode(newPassword));
    }
}
