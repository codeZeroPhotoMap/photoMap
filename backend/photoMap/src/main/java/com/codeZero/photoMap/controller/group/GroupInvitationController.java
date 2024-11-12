package com.codeZero.photoMap.controller.group;

import com.codeZero.photoMap.security.CustomUserDetails;
import com.codeZero.photoMap.service.group.GroupInvitationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/invitations")
public class GroupInvitationController {

    private final GroupInvitationService groupInvitationService;

    public GroupInvitationController(GroupInvitationService groupInvitationService) {
        this.groupInvitationService = groupInvitationService;
    }

    /**
     * 초대 수락 API
     * @param token 초대 토큰
     * @param userDetails  로그인한 사용자의 CustomUserDetails 객체(토큰에서 추출된 로그인한 사용자 정보)
     * @return ResponseEntity<String> 초대 수락 응답 메시지
     */
    @GetMapping("/accept")
    public ResponseEntity<String> acceptInvitation(
            @RequestParam String token,
            @AuthenticationPrincipal CustomUserDetails userDetails) {


        //로그인되지 않은 상태에서 요청이 오면 401 응답 반환
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        //로그인된 사용자라면 초대 수락 처리
        Long memberId = userDetails.getId();
        groupInvitationService.acceptInvitation(token, memberId);

        return ResponseEntity.ok("초대가 성공적으로 수락되었습니다.");

    }

}

