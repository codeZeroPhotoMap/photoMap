package com.codeZero.photoMap.controller.group;

import com.codeZero.photoMap.common.ApiResponse;
import com.codeZero.photoMap.dto.group.request.GroupTokenRequest;
import com.codeZero.photoMap.dto.group.response.GroupResponse;
import com.codeZero.photoMap.security.CustomUserDetails;
import com.codeZero.photoMap.service.group.GroupInvitationService;
import com.codeZero.photoMap.service.group.GroupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/invitations")
public class GroupInvitationController {

    private final GroupInvitationService groupInvitationService;
    private final GroupService groupService;


    public GroupInvitationController(GroupInvitationService groupInvitationService, GroupService groupService) {
        this.groupInvitationService = groupInvitationService;
        this.groupService = groupService;
    }

    /**
     * 초대 수락 API
     * @param request   그룹 초대 토큰 수락 요청 DTO
     * @param userDetails  로그인한 사용자의 CustomUserDetails 객체(토큰에서 추출된 로그인한 사용자 정보)
     * @return  ApiResponse 초대가 수락된 그룹의 정보 응답 DTO
     */
    @PostMapping("/accept")
    public ApiResponse<GroupResponse> acceptInvitation(
            @RequestBody  GroupTokenRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {


        //로그인되지 않은 상태에서 요청이 오면 401 응답 반환
        if (userDetails == null) {
            return ApiResponse.of(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.", null);
        }

        //로그인된 사용자라면 초대 수락 처리
        Long memberId = userDetails.getId();
        Long groupId = groupInvitationService.acceptInvitation(request.getGroupToken(), memberId);

        GroupResponse response = groupService.getGroup(groupId);
        return ApiResponse.ok(response);
    }

}

