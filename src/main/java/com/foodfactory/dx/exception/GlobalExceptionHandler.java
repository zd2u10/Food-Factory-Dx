package com.foodfactory.dx.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * アプリケーション全体で発生した例外(エラー)を、分かりやすいHTTPレスポンスに変換する仕組み。
 *
 * これが無い場合、Service層で throw した IllegalArgumentException は
 * Spring Bootのデフォルトのエラー画面(白い画面に難しいスタックトレースが表示される、
 * いわゆる「Whitelabel Error Page」)としてそのまま返ってしまい、
 * APIを呼び出す側(将来のReact画面やcurlでの動作確認)が原因を読み取りづらい。
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
     *   ProcurementServiceで書いた「検品結果の数量が入荷総量と一致しない場合」等のエラーは
     *   全てここでキャッチされ、以下の形に変換されて返る。
     *
     * HttpStatus.BAD_REQUEST(400番): 「リクエストの内容(送られてきたデータ)自体に
     *   問題があります」という意味のステータスコード。
     *   サーバー側の不具合(500番台)ではなく、入力値の不備であることを明示する。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
