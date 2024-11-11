package com.codeZero.photoMap.config;


import com.codeZero.photoMap.security.JwtAuthenticationFilter;
import com.codeZero.photoMap.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("https://goormfinal.vercel.app");
        configuration.addAllowedMethod("*"); //모든 HTTP 메서드 허용
        configuration.addAllowedHeader("*"); //모든 헤더 허용
        configuration.setAllowCredentials(true); //인증 정보 포함 여부

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf((auth) -> auth.disable());    // crsf 비활성화
        http.formLogin((auth) -> auth.disable());   //form로그인 비활성화
        http.httpBasic((auth) -> auth.disable());   //http basic 인증방식 비활성화
        http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));    //세션 설정

        //H2콘솔 허용 설정. 같은 origin에서 접근 허용
//        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        //api 접근 권한 설정
        http.authorizeHttpRequests((auth) -> auth
//                .requestMatchers(PathRequest.toH2Console()).permitAll() // h2console 접근 모두 허용
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll() //Preflight request 요청 모두 허용
                .requestMatchers( "/", "/api/members/login", "/api/members/login/kakao").permitAll()    //메인,로그인 모두 접근 허용
                .requestMatchers(HttpMethod.POST, "/api/members").permitAll()   //회원가입 접근 허용
                .requestMatchers(HttpMethod.PATCH, "/api/members/delete").authenticated() //탈퇴 허용
                .requestMatchers(HttpMethod.POST, "/api/groups/*/invite").authenticated() //초대URL
                .requestMatchers(HttpMethod.GET, "/api/invitations/accept").permitAll() // 초대 수락 URL GET 요청 허용
                .anyRequest().authenticated()); //나머지 요청은 로그인한 사용자만 접근 가능

        //예외 처리기 설정
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    String currentUri = request.getRequestURI();

                    //인증이 필요하지 않은 경로는 리다이렉트하지 않음(아래 조건들이 아니고, 인증이 안되어있다면 로그인으로 리다이렉트)
                    if (!(currentUri.equals("/") ||
                            (currentUri.startsWith("/api/members") && request.getMethod().equals("POST")) ||
                            currentUri.startsWith("/api/members/login") ||
                            currentUri.startsWith("/api/members/login/kakao") ||
                            currentUri.equals("/favicon.ico"))  //favicon.ico 요청은 무시
                        ) {

                        String query = request.getQueryString() != null ? "?" + request.getQueryString() : "";
                        String redirectUrl = currentUri + query;
                        response.sendRedirect("/api/members/login?redirect=" + URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8));
                    } else {
                        //허용된 경로는 리다이렉트하지 않음
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                })
        );


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
