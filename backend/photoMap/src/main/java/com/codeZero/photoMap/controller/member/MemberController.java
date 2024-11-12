package com.codeZero.photoMap.controller.member;

import com.codeZero.photoMap.api.KakaoLoginAPI;
import com.codeZero.photoMap.common.ApiResponse;
import com.codeZero.photoMap.common.exception.ForbiddenException;
import com.codeZero.photoMap.common.exception.NotFoundException;
import com.codeZero.photoMap.common.exception.UnauthorizedException;
import com.codeZero.photoMap.domain.member.Member;
import com.codeZero.photoMap.dto.member.request.*;
import com.codeZero.photoMap.dto.member.response.EmailCheckResponse;
import com.codeZero.photoMap.dto.member.response.KakaoUserInfoResponse;
import com.codeZero.photoMap.dto.member.response.MemberResponse;
import com.codeZero.photoMap.dto.member.response.TokenResponse;
import com.codeZero.photoMap.security.CustomUserDetails;
import com.codeZero.photoMap.security.JwtTokenProvider;
import com.codeZero.photoMap.service.member.MemberService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final KakaoLoginAPI kakaoLoginAPI;
    private final JwtTokenProvider jwtTokenProvider;

    public MemberController(MemberService memberService, KakaoLoginAPI kakaoLoginAPI, JwtTokenProvider jwtTokenProvider) {

        this.memberService = memberService;
        this.kakaoLoginAPI = kakaoLoginAPI;
        this.jwtTokenProvider = jwtTokenProvider;

    }


    /**
     * 회원가입 API
     * @param request 회원가입 요청 DTO
     * @return ApiResponse<String> 회원가입 성공 메시지
     */
    @PostMapping
    public ApiResponse<String> registerMember(@RequestBody MemberRequest request) {

        memberService.registerMember(request);
        return ApiResponse.ok("회원가입 성공");
    }

    /**
     * 이메일 중복 확인
     * @param email 중복여부를 확인할 email 주소
     * @return ApiResponse<EmailCheckResponse> 중복 여부와 메시지를 포함한 응답 DTO
     */
    @GetMapping("/check-email")
    public ApiResponse<EmailCheckResponse> checkEmailDuplicate(
            @RequestParam String email) {
        boolean isDuplicate = memberService.checkDuplicateEmail(email);
        String message = isDuplicate ? "이미 존재하는 이메일입니다" : "사용 가능한 이메일입니다";

        EmailCheckResponse response = EmailCheckResponse.builder()
                .isDuplicate(isDuplicate)
                .message(message)
                .build();

        return ApiResponse.ok(response);
    }

    /**
     * 로그인 API
     * @param request 로그인 요청 DTO
     * @return ResponseEntity<ApiResponse<TokenResponse>> 로그인 성공 시 액세스 토큰 및 리프레시 토큰을 포함한 응답 DTO
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @RequestBody LoginRequest request) {

        TokenResponse tokenResponse = memberService.login(request);

        //Authorization 헤더에 access 토큰 추가
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + tokenResponse.getAccessToken());

        //ApiResponse 형식으로 JSON 응답 생성
        ApiResponse<TokenResponse> response = ApiResponse.ok(tokenResponse);

        //헤더(accessToken포함)와 ApiResponse 응답 반환
        return ResponseEntity.ok().headers(headers).body(response);
    }

    /**
     * 카카오 회원가입 & 로그인 API
     * @param code 카카오에서 제공한 인증 코드
     * @param session HttpSession 객체
     * @return ResponseEntity<ApiResponse<String>> 바디에 로그인 성공 메시지, 헤더에 JWT 토큰
     */
    @GetMapping("/login/kakao")
    public ResponseEntity<ApiResponse<TokenResponse>> kakaoLogin(
            @RequestParam("code") String code, HttpSession session) {
        //1. 카카오에서 Access Token 가져오기
        String accessToken = kakaoLoginAPI.getAccessToken(code);

        //2. Access Token으로 사용자 정보 가져오기
        KakaoUserInfoResponse userInfo = kakaoLoginAPI.getUserInfo(accessToken);

        //3. 카카오 DB에서 사용자 정보 가져오기
        String email = userInfo.getEmail();
        String nickname = userInfo.getNickname();

        //이메일 값이 null인 경우 예외 처리
        if (email == null) {
            throw new IllegalArgumentException("이메일 정보를 불러오지 못했습니다. 카카오 로그인 설정을 확인하세요.");
        }

//        try {

            //4.회원 가입 또는 로그인 처리 후 JWT 액세스 및 리프레시 토큰 반환
            TokenResponse tokenResponse = memberService.processKakaoLogin(email, nickname);

            //Authorization 헤더에 토큰 추가
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + tokenResponse.getAccessToken());

            //JWT 토큰을 응답으로 반환
            ApiResponse<TokenResponse> response = ApiResponse.ok(tokenResponse);
//            return ResponseEntity.ok().headers(headers).body(response);

        //리디렉션해서 파라미터로 토큰 전달
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("https://goormfinal.vercel.app/map?accessToken=" + tokenResponse.getAccessToken() + "&refreshToken=" + tokenResponse.getRefreshToken()))
                .build();


//        } catch (ForbiddenException  e) {
//
//            //탈퇴된 회원인 경우 예외 메시지 반환
//            throw new ForbiddenException("탈퇴한 회원입니다. 로그인이 불가합니다.");
//
//        }
    }

    /**
     * 리프레시 토큰을 통해 새로운 accessToken,refreshToken 발급 API
     * @param request 리프레시 토큰을 담고 있는 요청 DTO
     * @return ResponseEntity<ApiResponse<TokenResponse>> - 새로 발급된 액세스 토큰과 리프레시 토큰을 포함한 응답 DTO
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshAccessToken(
            @RequestBody RefreshTokenRequest request) {
        if (jwtTokenProvider.validateToken(request.getRefreshToken())) {
            Claims claims = jwtTokenProvider.getClaims(request.getRefreshToken());
            String userId = claims.getSubject();

            String newAccessToken = jwtTokenProvider.createToken(userId);
            String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

            TokenResponse tokenResponse = TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();

            return ResponseEntity.ok(ApiResponse.of(HttpStatus.OK, "토큰 갱신 성공", tokenResponse));

        } else {
            throw new UnauthorizedException("리프레시 토큰이 만료되었거나 유효하지 않습니다.");
        }
    }

    /**
     * 로그인한 회원 정보 조회
     * @param userDetails 로그인한 사용자의 CustomUserDetails 객체
     * @return ApiResponse<MemberResponse> 맴버 정보 응답 DTO
     */
    @GetMapping("/info")
    public ApiResponse<MemberResponse> getMemberInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new NotFoundException("인증된 사용자 정보가 없습니다.");
        }

        MemberResponse memberInfo = memberService.getMemberInfo(userDetails.getUsername());

        return ApiResponse.ok(memberInfo);
    }

    /**
     * 이름 변경 API
     * @param userDetails 로그인한 사용자의 CustomUserDetails 객체
     * @param request 이름 변경 요청 DTO
     * @return ApiResponse<MemberResponse> 변경된 멤버 정보 응답 DTO
     */
    @PatchMapping("/name")
    public ApiResponse<MemberResponse> updateName(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody NameUpdateRequest request) {

        MemberResponse response = memberService.updateName(userDetails.getId(), request);

        return ApiResponse.ok(response);
    }

    /**
     * 비밀번호 변경 API
     * @param userDetails 로그인한 사용자의 CustomUserDetails 객체
     * @param request 비밀번호 변경 요청 DTO
     * @return ApiResponse<MemberResponse> 변경된 멤버 정보 DTO
     */
    @PatchMapping("/password")
    public ApiResponse<MemberResponse> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PasswordUpdateRequest request) {

        Member member = memberService.findMemberById(userDetails.getId());

        //카카오 사용자는 비번 변경 불가
        if (member.isSocialLogin()) {
            throw new ForbiddenException("카카오 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
        }

        MemberResponse response = memberService.updatePassword(userDetails.getId(), request);

        return ApiResponse.ok(response);
    }

    /**
     * 회원 탈퇴 API(소프트 딜리트 사용)
     * @param userDetails 로그인한 사용자의 CustomUserDetails 객체
     * @return ApiResponse - 탈퇴 성공 메시지를 포함한 응답 객체, HTTPStatus 204
     */
    @PatchMapping("/delete")
    public ApiResponse<String> deleteMember(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

            memberService.deleteMember(userDetails.getId());

        return ApiResponse.of(HttpStatus.NO_CONTENT, "회원 탈퇴 성공");
    }
}

