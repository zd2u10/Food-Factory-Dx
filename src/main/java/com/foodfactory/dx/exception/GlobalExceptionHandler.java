package com.foodfactory.dx.exception;

import com.foodfactory.dx.config.AuthUtil;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * アプリケーション全体で発生した例外(エラー)を、分かりやすいHTTPレスポンスに変換する仕組み。
 *
 * これが無い場合、Service層で throw した例外は
 * Spring Bootのデフォルトのエラー画面(白い画面に難しいスタックトレースが表示される、
 * いわゆる「Whitelabel Error Page」)としてそのまま返ってしまい、
 * APIを呼び出す側(将来のReact画面やThunder Clientでの動作確認)が原因を読み取りづらい。
 *
 * @RestControllerAdvice: 「ここに書いたルールを、アプリ全体の全Controllerに対して
 *                        横断的に適用してください」という意味の目印。
 *                        個々のControllerに同じエラー処理を毎回書く必要がなくなる。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @ExceptionHandler(IllegalArgumentException.class):
     *   「IllegalArgumentExceptionが投げられたら、このメソッドで処理してください」という指定。
     *   「検品結果の数量が一致しない」「材料の在庫が不足している」といった
     *   “入力値そのものに問題がある”エラーはここでキャッチする。
     *
     * HttpStatus.BAD_REQUEST(400番): 「リクエストの内容(送られてきたデータ)自体に
     *   問題があります」という意味のステータスコード。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * IllegalStateException: 入力値そのものは正しいが、
     * 「今このタイミングでその操作をするのは状態的におかしい」というエラー用。
     * 例: まだDRAFTのバッチに対して、いきなり完了処理を呼んでしまった場合など。
     *
     * HttpStatus.CONFLICT(409番): 「リクエスト自体は正しい形式だが、
     *   現在のリソースの状態と矛盾しています」という意味のステータスコード。
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * 権限レベル不足のエラー。403 Forbiddenを返し、要求レベル・現在のレベルも
     * レスポンスに含める(フロント側で「スロー処理」の確認画面を組み立てるために使う。
     * 要件定義書8.28節を参照)。
     */
    @ExceptionHandler(AuthUtil.InsufficientAccessLevelException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientAccessLevel(AuthUtil.InsufficientAccessLevelException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "error", ex.getMessage(),
                        "requiredLevel", ex.getRequiredLevel(),
                        "currentLevel", ex.getCurrentLevel()
                ));
    }
}
