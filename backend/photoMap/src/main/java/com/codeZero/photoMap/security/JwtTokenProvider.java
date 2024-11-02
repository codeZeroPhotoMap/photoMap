package com.codeZero.photoMap.security;

import com.codeZero.photoMap.common.exception.ForbiddenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private Key SECRET_KEY;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @PostConstruct
    protected void init() {
        SECRET_KEY = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    private final UserDetailsService userDetailsService;

    public JwtTokenProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    //JWT 토큰 생성
    public String createToken(String username) {
        //현재 시간
        Instant now = Instant.now();
        //1시간 뒤 유효 만료 시간 설정
        Instant validity = now.plus(1, ChronoUnit.HOURS);

        return Jwts.builder()
                .setSubject(username) //주체(subject)를 바로 설정
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(validity))
                .signWith(SECRET_KEY)
                .compact();
    }

    //토큰에서 인증 정보 조회
    public Authentication getAuthentication(String token) {
        String username = getUsername(token); //토큰에서 사용자 이름 추출
        UserDetails userDetails = userDetailsService.loadUserByUsername(username); // UserDetailsService로 사용자 정보 로드
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    //토큰에서 사용자 이름 추출
    public String getUsername(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY) //String 타입 키 사용
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

    }

    //Request 요청의 Authorization 헤더에서 토큰 추출. "Authorization" : "Bearer TOKEN"
    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    //토큰의 유효성 + 만료일자 확인
    public boolean validateToken(String token) {

        try {
            Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);

            return true;

        } catch (Exception e) {
            throw new ForbiddenException("유효하지 않거나 만료된 토큰입니다"); // ForbiddenException 발생

//            return false;

        }
    }
}
