package com.foodfactory.dx.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ブラウザのCORS(Cross-Origin Resource Sharing)制限に対応するための設定。
 *
 * ブラウザは仕様上、「今表示しているページのポート」と「通信先のポート」が違うと、
 * デフォルトで通信を拒否する(セキュリティ上の仕組み)。
 * 今回はReact開発サーバー(http://localhost:5173)から、
 * Spring Boot(http://localhost:8080)へ通信する必要があるため、
 * 5173番からのアクセスを明示的に許可する設定をここに書く。
 *
 * 【注意】これは開発環境向けの設定。本番環境で公開する際は、
 * 許可するオリジン(allowedOrigins)を実際に配信するドメインに絞り込む必要がある。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
