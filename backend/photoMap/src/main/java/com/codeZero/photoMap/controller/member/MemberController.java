package com.codeZero.photoMap.controller.member;

import com.codeZero.photoMap.common.ApiResponse;
import com.codeZero.photoMap.common.exception.NotFoundException;
import com.codeZero.photoMap.dto.member.request.*;
import com.codeZero.photoMap.dto.member.response.MemberResponse;
import com.codeZero.photoMap.security.CustomUserDetails;
import com.codeZero.photoMap.service.member.MemberService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
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
     * 로그인 API
     * @param request 로그인 요청 DTO
     * @return ResponseEntity<ApiResponse<String>> 바디에 로그인 성공 메시지, 헤더에 JWT 토큰
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest request) {
        String token = memberService.login(request);

        //Authorization 헤더에 토큰 추가
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token); // 토큰을 Authorization 헤더에 추가

        //ApiResponse 형식으로 JSON 응답 생성
        ApiResponse<String> response = ApiResponse.ok("로그인 성공");

        //헤더와 ApiResponse 응답 반환
        return ResponseEntity.ok().headers(headers).body(response);
    }

    /**
     * 카카오 로그인 API 아직
     * @return ApiResponse<String> 카카오 로그인 성공 메시지
     */
//    @GetMapping("/login/kakao")
//    public ApiResponse<String> kakaoLogin() {
//        String result = memberService.kakaoLogin();
//        return ApiResponse.ok(result);
//    }


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

