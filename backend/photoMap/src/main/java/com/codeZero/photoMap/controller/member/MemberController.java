package com.codeZero.photoMap.controller.member;

import com.codeZero.photoMap.api.KakaoLoginAPI;
import com.codeZero.photoMap.common.ApiResponse;
import com.codeZero.photoMap.common.exception.ForbiddenException;
import com.codeZero.photoMap.common.exception.NotFoundException;
import com.codeZero.photoMap.dto.member.request.*;
import com.codeZero.photoMap.dto.member.response.KakaoUserInfoResponse;
import com.codeZero.photoMap.dto.member.response.MemberResponse;
import com.codeZero.photoMap.security.CustomUserDetails;
import com.codeZero.photoMap.service.member.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final KakaoLoginAPI kakaoLoginAPI;

    public MemberController(MemberService memberService, KakaoLoginAPI kakaoLoginAPI) {

        this.memberService = memberService;
        this.kakaoLoginAPI = kakaoLoginAPI;

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

//    //TODO : test용
//    @GetMapping("/login")
//    public String showLoginPage() {
//        return "로그인 페이지 반환.";
//    }

    /**
     * 로그인 API
     * @param request 로그인 요청 DTO
     * @return ResponseEntity<ApiResponse<String>> 바디에 로그인 성공 메시지, 헤더에 JWT 토큰
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(
            @RequestBody LoginRequest request,
            @RequestParam(value = "redirect", required = false) String redirectUrl, // @RequestParam으로 URL 쿼리 파라미터 가져오기
            HttpServletRequest httpRequest) {

        String token = memberService.login(request);

        //Authorization 헤더에 토큰 추가
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token); // 토큰을 Authorization 헤더에 추가

        //원래 가려던 URL가져와서 리다이렉트URL 응답 처리
        if (redirectUrl != null && !redirectUrl.isEmpty() && !redirectUrl.equals("/api/members/login")) {

            return ResponseEntity.status(HttpStatus.FOUND)  //302 Found(브라우저에 리다이렉트 지시)
                    .header(HttpHeaders.LOCATION, redirectUrl)  //LOCATION 사용자가 리다이렉트 될 경로
                    .headers(headers)   //헤더에 토큰 추가
                    .build();
        }

        //ApiResponse 형식으로 JSON 응답 생성
        ApiResponse<String> response = ApiResponse.ok("로그인 성공");

        //헤더와 ApiResponse 응답 반환
        return ResponseEntity.ok().headers(headers).body(response);
    }

    /**
     * 카카오 회원가입 & 로그인 API
     * @param code 카카오에서 제공한 인증 코드
     * @param session HttpSession 객체
     * @return ResponseEntity<ApiResponse<String>> 바디에 로그인 성공 메시지, 헤더에 JWT 토큰
     */
    @GetMapping("/login/kakao")
    public ResponseEntity<ApiResponse<String>> kakaoLogin(
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

        try {

            //4.회원 가입 또는 로그인 처리 후 JWT 토큰 반환
            String jwtToken = memberService.processKakaoLogin(email, nickname);

            //TODO : test용, 삭제예정
            System.out.println("jwtToken: Bearer " + jwtToken);

            //Authorization 헤더에 토큰 추가
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + jwtToken);

            //JWT 토큰을 응답으로 반환
            ApiResponse<String> response = ApiResponse.ok("카카오 로그인 성공");
            return ResponseEntity.ok().headers(headers).body(response);

        } catch (ForbiddenException  e) {

            //탈퇴된 회원인 경우 예외 메시지 반환
            throw new ForbiddenException("탈퇴한 회원입니다. 로그인이 불가합니다.");

        }
    }

    /**
     * 로그인한 회원 정보 조회
     * @param userDetails 로그인한 사용자의 CustomUserDetails 객체
     * @return ApiResponse<MemberResponse> 회원 정보
     */
    @GetMapping
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
     * @return 이름 변경 성공 메시지, 변경된 멤버 정보 DTO
     */
    @PatchMapping("/name")
    public ApiResponse<MemberResponse> updateName(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody NameUpdateRequest request) {

        MemberResponse response = memberService.updateName(userDetails.getId(), request);

        return ApiResponse.of(HttpStatus.OK, "이름 변경 성공", response);
    }

    /**
     * 비밀번호 변경 API
     * @param userDetails 로그인한 사용자의 CustomUserDetails 객체
     * @param request 비밀번호 변경 요청 DTO
     * @return 비밀번호 변경 성공 메시지, 변경된 멤버 정보 DTO
     */
    @PatchMapping("/password")
    public ApiResponse<MemberResponse> updatePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PasswordUpdateRequest request) {

        MemberResponse response = memberService.updatePassword(userDetails.getId(), request);

        return ApiResponse.of(HttpStatus.OK, "비밀번호 변경 성공", response);
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

