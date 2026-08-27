package com.foodfactory.dx.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * ブラウザのCORS(Cross-Origin Resource Sharing)制限に対応するための設定。
 *
 * ブラウザは仕様上、「今表示しているページのポート」と「通信先のポート」が違うと、
 * デフォルトで通信を拒否する(セキュリティ上の仕組み)。
 * 今回はReact開発サーバー(http://localhost:5173)から、
 * Spring Boot(http://localhost:8080)へ通信する必要があるため、
 * 5173番からのアクセスを明示的に許可する設定をここに書く。
 *
 * 【設計変更】以前は WebMvcConfigurer の addCorsMappings で設定していたが、
 * Spring Securityを導入すると、リクエストが最初にSpring Securityのフィルターを
 * 通過するため、WebMvcConfigurer側のCORS設定だけでは、Spring Securityに
 * よってリクエストがブロックされてしまう(実際に、この設定漏れにより
 * 「No 'Access-Control-Allow-Origin' header」エラーが発生した)。
 * そのため、Spring Securityが直接認識できる CorsConfigurationSource という
 * 形式のBeanとして定義し、SecurityConfig側で .cors(withDefaults()) を通じて
 * 明示的に使用するようにしている(要件定義書8.27節を参照)。
 *
 * セッションベース認証では、ログイン状態をCookie(JSESSIONID)でやり取りする。
 * ブラウザは、異なるオリジン間でCookieを送受信することを、デフォルトでは
 * 許可しないため、setAllowCredentials(true)を明示的に指定する必要がある。
 * また、allowedOrigins に "*"(ワイルドカード)は、allowCredentials(true)と
 * 同時には使えない(ブラウザの仕様)ため、具体的なオリジンを1つずつ指定する。
 *
 * 【注意】これは開発環境向けの設定。本番環境で公開する際は、
 * 許可するオリジン(allowedOrigins)を実際に配信するドメインに絞り込む必要がある。
 */
@Configuration
public class WebConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
