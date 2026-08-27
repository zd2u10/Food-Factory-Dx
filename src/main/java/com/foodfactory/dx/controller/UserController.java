package com.foodfactory.dx.controller;

import com.foodfactory.dx.config.AuthUtil;
import com.foodfactory.dx.domain.User;
import com.foodfactory.dx.dto.CreateUserRequest;
import com.foodfactory.dx.dto.ResetPasswordRequest;
import com.foodfactory.dx.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ユーザーアカウント管理API。新規登録・無効化は、権限レベル3(管理者)専用
 * (要件定義書8.27節を参照)。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthUtil authUtil;

    public UserController(UserService userService, AuthUtil authUtil) {
        this.userService = userService;
        this.authUtil = authUtil;
    }

    /** ユーザー一覧の閲覧は、レベル3(管理者)のみ許可する(他ユーザーの権限レベルが見えるため)。 */
    @GetMapping
    public List<User> list(HttpServletRequest request) {
        authUtil.requireLevel(request, 3);
        return userService.listUsers();
    }

    @PostMapping
    public ResponseEntity<User> create(HttpServletRequest request, @RequestBody CreateUserRequest createRequest) {
        authUtil.requireLevel(request, 3);
        User created = userService.createUser(
                createRequest.getUsername(), createRequest.getPassword(), createRequest.getAccessLevel());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{userId}/deactivate")
    public ResponseEntity<Void> deactivate(HttpServletRequest request, @PathVariable Long userId) {
        authUtil.requireLevel(request, 3);
        userService.setActive(userId, false);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/activate")
    public ResponseEntity<Void> activate(HttpServletRequest request, @PathVariable Long userId) {
        authUtil.requireLevel(request, 3);
        userService.setActive(userId, true);
        return ResponseEntity.ok().build();
    }

    /**
     * 管理者(レベル3)が、他ユーザーのパスワードを直接、新しいものに上書きする
     * (登録時の入力ミスや、パスワードを忘れた場合の訂正用)。
     */
    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<Void> resetPassword(
            HttpServletRequest request, @PathVariable Long userId, @RequestBody ResetPasswordRequest resetRequest) {
        authUtil.requireLevel(request, 3);
        userService.resetPassword(userId, resetRequest.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
