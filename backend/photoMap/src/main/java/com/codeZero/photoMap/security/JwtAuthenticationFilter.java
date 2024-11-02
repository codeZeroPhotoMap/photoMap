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

        try {
            String token = jwtTokenProvider.resolveToken(request); //Request에서 토큰을 가져옴

            //토큰이 유효한 경우, 인증 설정
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);

            }
            chain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException e) {
            throw new ForbiddenException("유효하지 않거나 만료된 토큰입니다"); //ForbiddenException 발생
        }
    }
}
