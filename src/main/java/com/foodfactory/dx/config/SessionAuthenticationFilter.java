package com.foodfactory.dx.config;

import com.foodfactory.dx.domain.User;
import com.foodfactory.dx.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * HttpSessionに保存されているログイン情報(AuthService.SESSION_KEY_USER)を、
 * リクエストのたびに、Spring SecurityのSecurityContext(そのリクエストの間だけ有効な、
 * 「今誰がログインしているか」を表す仕組み)に反映するフィルター。
 *
 * これが無いと、SecurityConfigの .authenticated() が、常に「未ログイン」と
 * 判定してしまい、ログイン後もAPIが呼べなくなる(要件定義書8.27節を参照)。
 */
@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        User user = (User) (request.getSession(false) != null
                ? request.getSession(false).getAttribute(AuthService.SESSION_KEY_USER)
                : null);

        if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
