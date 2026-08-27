package com.foodfactory.dx.controller;

import com.foodfactory.dx.domain.User;
import com.foodfactory.dx.dto.ChangePasswordRequest;
import com.foodfactory.dx.dto.LoginRequest;
import com.foodfactory.dx.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ログイン・ログアウト・現在ユーザー取得・パスワード変更のAPI(要件定義書8.27節を参照)。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public User login(HttpServletRequest request, @RequestBody LoginRequest loginRequest) {
        return authService.login(request, loginRequest.getUsername(), loginRequest.getPassword());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    /** 現在ログイン中のユーザーを取得する。未ログインの場合はnull(200で返す。401ではない)。 */
    @GetMapping("/me")
    public User me(HttpServletRequest request) {
        return authService.getCurrentUser(request);
    }

    /**
     * パスワードを変更する。現在のパスワードが一致することを、AuthService側で必ず確認する
     * (「元のパスワードを知らない人間は書き換えられない」という要件を満たすため)。
     */
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(HttpServletRequest request, @RequestBody ChangePasswordRequest changeRequest) {
        User currentUser = authService.getCurrentUser(request);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        authService.changePassword(request, currentUser.getUserId(),
                changeRequest.getCurrentPassword(), changeRequest.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
