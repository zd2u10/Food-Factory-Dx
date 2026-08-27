package com.foodfactory.dx.service;

import com.foodfactory.dx.domain.User;
import com.foodfactory.dx.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * ログイン・ログアウト・現在ログイン中のユーザー取得に関するService。
 *
 * 【設計】Spring Securityの UserDetails/UserDetailsService は使わず、
 * ログイン成功時に、HttpSession へ直接 User オブジェクトを保存するシンプルな方式にしている
 * (今回の規模(社内10名程度)には、フル機能のUserDetails実装はオーバースペックと判断)。
 * セッションに保存された情報は、AuthUtil経由で、各Controllerから参照する
 * (要件定義書8.27節を参照)。
 */
@Service
public class AuthService {

    /** セッションに、ログイン中のユーザー情報を保存する際のキー。 */
    public static final String SESSION_KEY_USER = "LOGIN_USER";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * ログインを試みる。成功したら、セッションにユーザー情報を保存する。
     * 失敗した場合(ユーザー名不一致、パスワード不一致、無効化されたアカウント)は、
     * 全て同じメッセージの例外にする(「ユーザー名は合っているが、パスワードが違う」
     * といった、攻撃のヒントになる情報を、レスポンスから読み取れないようにするため)。
     */
    public User login(HttpServletRequest request, String username, String password) {
        User user = userMapper.findByUsername(username)
                .filter(User::isActive)
                .filter(u -> passwordEncoder.matches(password, u.getPasswordHash()))
                .orElseThrow(() -> new IllegalArgumentException("ユーザー名またはパスワードが正しくありません。"));

        // セッション固定攻撃(session fixation)対策として、ログイン成功時にセッションIDを
        // 振り直す(既存セッションを破棄し、新しいセッションを作る)。
        request.getSession().invalidate();
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute(SESSION_KEY_USER, user);

        return user;
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /** 現在ログイン中のユーザーを取得する(未ログインならnull)。 */
    public User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute(SESSION_KEY_USER);
    }

    /**
     * パスワードを変更する。現在のパスワードが一致することを、必ず確認してから変更する
     * (「元のパスワードを知らない人間は書き換えられない」という要件を、確実に満たすため)。
     */
    public void changePassword(HttpServletRequest request, Long userId, String currentPassword, String newPassword) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("指定されたユーザーが見つかりません: userId=" + userId));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("現在のパスワードが正しくありません。");
        }
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 8) {
            throw new IllegalArgumentException("新しいパスワードは8文字以上で入力してください。");
        }

        String newHash = passwordEncoder.encode(newPassword);
        userMapper.updatePasswordHash(userId, newHash);

        // セッションに保存されているユーザー情報も、念のため更新しておく
        // (パスワードハッシュ自体は画面に表示されないが、整合性のため)。
        HttpSession session = request.getSession(false);
        if (session != null) {
            User sessionUser = (User) session.getAttribute(SESSION_KEY_USER);
            if (sessionUser != null && sessionUser.getUserId().equals(userId)) {
                sessionUser.setPasswordHash(newHash);
            }
        }
    }
}
