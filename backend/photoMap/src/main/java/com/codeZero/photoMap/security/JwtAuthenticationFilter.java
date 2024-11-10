package com.codeZero.photoMap.security;


import com.codeZero.photoMap.common.exception.ForbiddenException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String path = request.getRequestURI();
        //TODO : test
        System.out.println("Request URI: " + path); // 요청URI 로그
        //특정 경로 필터링을 건너뛰도록 설정
        if (path.equals("/") || path.equals("/api/members") || path.equals("/api/members/login") || path.equals("/api/members/login/kakao")) {
            //TODO : test
            System.out.println("Skipping filter for path: " + path); // 필터건너뜀 로그
            chain.doFilter(request, response);
            return;
        }

        try {
            // JWT 토큰 검증 로직
            String token = jwtTokenProvider.resolveToken(request);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            //JWT 로그
            System.err.println("JWT 인증 오류: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT 인증 오류: " + e.getMessage());
            return;
        }

        chain.doFilter(request, response);

    }
}
