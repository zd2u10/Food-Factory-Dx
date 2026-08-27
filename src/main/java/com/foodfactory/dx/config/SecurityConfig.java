package com.foodfactory.dx.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Securityの基本設定(要件定義書8.27節を参照)。
 *
 * 【設計方針】「ログインしているかどうか」は、ここ(URLパターン単位)でチェックする。
 * 「どの権限レベル(1〜3)が必要か」という、より細かい判定は、各Controllerメソッドの中で
 * AuthUtil経由で個別に行う(URLパターンだけでは、「GETは誰でもよいがPOSTはレベル2以上」
 * のような、操作ごとに異なる粒度の制御が難しいため)。
 *
 * セッションベース認証を採用しているため、CSRF保護は一旦無効化している
 * (今回はAPIをJSONで叩く構成であり、フォーム送信を想定していないため。
 * 本来はCSRFトークンによる保護が望ましいが、今回の規模・運用(社内10名程度、
 * イントラネット的な利用)を踏まえ、優先度を下げている)。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SessionAuthenticationFilter sessionAuthenticationFilter)
            throws Exception {
        http
                // Spring Securityが、リクエストを一番最初に受け取る仕組みになるため、
                // ここで明示的に .cors() を有効にしないと、WebConfig(通常のSpring MVCの
                // CORS設定)が適用される前に、Spring Securityがブロックしてしまう。
                // withDefaults() を指定すると、WebConfig で定義した CorsConfigurationSource
                // (allowedOrigins・allowCredentials等)を、そのまま使ってくれる。
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ログインAPI・現在ユーザー確認APIは、未ログインでも呼べる必要がある。
                        // 特に /api/auth/me は、「今ログインしているか」を確認するためのAPIであり、
                        // 未ログイン状態で呼ばれることを前提としている(AuthController側でnullを
                        // 返す設計)。ここで除外しないと、Spring Security自体が、401として
                        // リクエストをブロックしてしまい、AuthControllerまで到達しなかった。
                        .requestMatchers("/api/auth/login", "/api/auth/me").permitAll()
                        // それ以外の /api/** は、全てログイン必須
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                // 未ログイン状態でAPIを呼んだ場合、ログイン画面へのリダイレクトではなく、
                // 401 Unauthorized を返す(フロントエンドはSPAのため、リダイレクトは不適切)。
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // Spring Securityの標準フォームログイン画面は使わず、独自のログインAPIを実装するため無効化。
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // セッションの中身を、Spring SecurityのSecurityContextに反映するフィルターを追加。
                .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
