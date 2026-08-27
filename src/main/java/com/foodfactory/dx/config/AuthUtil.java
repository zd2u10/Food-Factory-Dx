package com.foodfactory.dx.config;

import com.foodfactory.dx.domain.User;
import com.foodfactory.dx.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * 権限レベルのチェックを、各Controllerから共通で行うためのユーティリティ
 * (要件定義書8.27節を参照)。
 *
 * 【使い方】各Controllerのメソッドの冒頭で、
 *   authUtil.requireLevel(request, 2);
 * のように呼ぶ。権限が不足している場合は、InsufficientAccessLevelException を投げる
 * (この例外は、GlobalExceptionHandler等で、403 Forbiddenとして処理する想定)。
 */
@Component
public class AuthUtil {

    private final AuthService authService;

    public AuthUtil(AuthService authService) {
        this.authService = authService;
    }

    /** 現在ログイン中のユーザーを取得する。未ログインならnull。 */
    public User getCurrentUser(HttpServletRequest request) {
        return authService.getCurrentUser(request);
    }

    /**
     * 指定した権限レベル以上を要求する。未ログイン、または権限不足の場合、例外を投げる。
     */
    public User requireLevel(HttpServletRequest request, int requiredLevel) {
        User user = authService.getCurrentUser(request);
        if (user == null) {
            throw new InsufficientAccessLevelException("ログインが必要です。", requiredLevel, 0);
        }
        if (user.getAccessLevel() < requiredLevel) {
            throw new InsufficientAccessLevelException(
                    "この操作には、権限レベル" + requiredLevel + "以上が必要です。", requiredLevel, user.getAccessLevel());
        }
        return user;
    }

    /** 権限不足の際に投げる例外。要求レベル・現在のレベルを、フロントに伝えるために保持する。 */
    public static class InsufficientAccessLevelException extends RuntimeException {
        private final int requiredLevel;
        private final int currentLevel;

        public InsufficientAccessLevelException(String message, int requiredLevel, int currentLevel) {
            super(message);
            this.requiredLevel = requiredLevel;
            this.currentLevel = currentLevel;
        }

        public int getRequiredLevel() {
            return requiredLevel;
        }

        public int getCurrentLevel() {
            return currentLevel;
        }
    }
}
