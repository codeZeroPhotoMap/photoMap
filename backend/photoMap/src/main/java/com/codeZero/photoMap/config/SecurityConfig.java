package com.codeZero.photoMap.config;

import com.codeZero.photoMap.common.exception.ForbiddenException;
import com.codeZero.photoMap.security.JwtAuthenticationFilter;
import com.codeZero.photoMap.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity  //스프링시큐리티필터가 스프링필터체인에 등록됨
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    //비밀번호 암호화
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf((auth) -> auth.disable());    // crsf 비활성화
        http.formLogin((auth) -> auth.disable());   //form로그인 비활성화
        http.httpBasic((auth) -> auth.disable());   //http basic 인증방식 비활성화
        http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));    //세션 설정

        //예외처리기
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    throw new RuntimeException("인증이 필요합니다."); //예외를 던짐
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    throw new ForbiddenException("접근 권한이 없습니다."); //사용자 정의 예외를 던짐
                })
        );

        //H2콘솔 허용 설정. 같은 origin에서 접근 허용
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));


        //api 접근 권한 설정
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers(PathRequest.toH2Console()).permitAll() // h2console 접근 모두 허용
                .requestMatchers("/", "/api/members", "/api/members/login", "/api/members/login/kakao").permitAll()    //메인,로그인,회원가입페이지 모두 접근 허용
                .requestMatchers(HttpMethod.PATCH, "/api/members/delete").authenticated() //탈퇴 허용
                .requestMatchers(HttpMethod.POST, "/api/groups/*/invite").authenticated() //초대URL
                .anyRequest().authenticated()); //나머지 요청은 로그인한 사용자만 접근 가능

        http
                //로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/api/members/logout") // 로그아웃 URL
                        .logoutSuccessUrl("/") // 로그아웃 성공 시 리디렉션할 페이지
                        .permitAll()
                );

        //JWT필터
        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
}
