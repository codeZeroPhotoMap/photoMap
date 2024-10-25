package com.codeZero.photoMap.controller.member;

import com.codeZero.photoMap.domain.member.Member;
import com.codeZero.photoMap.dto.member.request.MemberCreateRequest;
import com.codeZero.photoMap.dto.member.response.MemberResponse;
import com.codeZero.photoMap.service.member.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 멤버 생성 테스트용!
     * @param request 멤버 생성 요청 DTO
     * @return
     */
        @PostMapping
        public ResponseEntity<Member> createMember(
                @RequestBody @Valid MemberCreateRequest request
        ) {
            Member member = memberService.createMember(request.getEmail(), request.getPassword(), request.getName());
            MemberResponse response = MemberResponse.from(member);

            return ResponseEntity.status(HttpStatus.CREATED).body(member);
        }
    }

