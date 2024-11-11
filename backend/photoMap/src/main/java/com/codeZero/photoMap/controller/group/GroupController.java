package com.codeZero.photoMap.controller.group;

import com.codeZero.photoMap.common.ApiResponse;
import com.codeZero.photoMap.dto.group.request.GroupCreateRequest;
import com.codeZero.photoMap.dto.group.request.GroupInvitationRequest;
import com.codeZero.photoMap.dto.group.request.GroupUpdateRequest;
import com.codeZero.photoMap.dto.group.response.GroupInvitationResponse;
import com.codeZero.photoMap.dto.group.response.GroupResponse;
import com.codeZero.photoMap.security.CustomUserDetails;
import com.codeZero.photoMap.service.group.GroupService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * 그룹 생성 API
     * @param request   그룹 생성 요청 DTO
     * @param userDetails   인증된 사용자의 정보가 포함된 객체(토큰에서 추출된 로그인한 사용자 정보)
     * @return ApiResponse<GroupResponse>   생성된 그룹 응답 DTO
     */
    @PostMapping
    public ApiResponse<GroupResponse> createGroup(
            @RequestBody GroupCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails  //토큰에서 memberId추출
    ) {

        Long memberId = userDetails.getId();
        GroupResponse response = groupService.createGroup(request, memberId);

        return ApiResponse.ok(response);
    }

    /**
     * 그룹 조회 API(로그인한 멤버가 가진 그룹 조회)
     * @param userDetails   인증된 사용자의 정보가 포함된 객체(토큰에서 추출된 로그인한 사용자 정보)
     * @return ApiResponse<List < GroupResponse>>   로그인한 멤버가 속한 모든 그룹 응답 DTO 목록
     */
    @GetMapping
    public ApiResponse<List<GroupResponse>> getGroupByMember(
            @AuthenticationPrincipal CustomUserDetails userDetails  //토큰에서 memberId추출
    ) {

        Long memberId = userDetails.getId();
        List<GroupResponse> responses = groupService.getGroupByMember(memberId);

        return ApiResponse.ok(responses);
    }

    /**
     * 그룹 조회 API(하나의 그룹이 가진 그룹정보,멤버 조회)
     * @param groupId   조회할 그룹Id
     * @return ApiResponse<GroupResponse>   요청 그룹의 정보와 멤버 목록 응답 DTO
     */
    @GetMapping("/{groupId}")
    public ApiResponse<GroupResponse> getGroup(
            @PathVariable Long groupId
    ) {

        GroupResponse response = groupService.getGroup(groupId);
        return ApiResponse.ok(response);

    }

    /**
     * 그룹 수정
     * @param groupId     수정할 그룹Id
     * @param request     그룹 수정 요청 DTO
     * @param userDetails 인증된 사용자의 정보가 포함된 객체(토큰에서 추출된 로그인한 사용자 정보)
     * @return ApiResponse<GroupResponse> 수정된 그룹 정보 응답 DTO
     */
    @PatchMapping("/{groupId}")
    public ApiResponse<GroupResponse> updateGroup(
            @PathVariable Long groupId,
            @RequestBody GroupUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails  //토큰에서 memberId추출

    ) {

        Long memberId = userDetails.getId();
        GroupResponse response = groupService.updateGroup(groupId, memberId, request);

        return ApiResponse.ok(response);

    }

    /**
     * 그룹 소프트 삭제 API
     * @param groupId   삭제할 그룹 ID
     * @return ApiResponse<GroupResponse>   삭제된 그룹 정보 응답 DTO(HTTPStatus 204)
     */
    @PatchMapping("/{groupId}/delete")
    public ApiResponse<Void> deleteGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Long ownerId = userDetails.getId();
        groupService.deleteGroup(groupId, ownerId);

        return ApiResponse.of(HttpStatus.NO_CONTENT, null);
    }

    /**
     * 초대 요청 API
     * @param groupId     초대할 그룹Id
     * @param request     초대받을 멤버의 이메일이 포함된 초대 요청 DTO
     * @param userDetails 인증된 사용자의 정보가 포함된 객체(토큰에서 추출된 로그인한 사용자 정보)
     * @return ApiResponse<String> 초대 요청 생성 완료 응답
     */
    @PostMapping("/{groupId}/invite")
    public ApiResponse<GroupInvitationResponse> createInvitation(
            @PathVariable Long groupId,
            @RequestBody GroupInvitationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();

        //현재 사용자가 그룹의 소유자인지 확인
        groupService.verifyGroupOwner(groupId, memberId);

        String token = groupService.createInvitation(request.getEmail(), groupId, memberId);
        GroupInvitationResponse response = GroupInvitationResponse.builder()
                .message("초대장이 성공적으로 발송되었습니다.")
                .token(token)
                .build();

        return ApiResponse.of(HttpStatus.CREATED, response);
    }

    /**
     * 그룹에서 멤버 삭제 API
     * @param groupId     멤버를 삭제할 그룹 ID
     * @param memberId    삭제할 멤버 ID
     * @param userDetails 로그인한 사용자의 정보가 포함된 CustomUserDetails 객체 (토큰에서 추출된 사용자 정보)
     * @return ApiResponse<Void> 삭제 완료 응답
     */
    @PatchMapping("/{groupId}/members/{memberId}")
    public ApiResponse<Void> removeMemberFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        //로그인한 사용자의 ID 가져오기
        Long ownerId = userDetails.getId();

        //그룹에서 멤버 삭제 서비스 호출 (그룹장 권한 확인 포함)
        groupService.removeMemberFromGroup(groupId, memberId, ownerId);

        return ApiResponse.of(HttpStatus.NO_CONTENT, null);
    }

    /**
     * 그룹에서 나가기
     * @param groupId   나가길 원하는 그룹Id
     * @param userDetails   로그인한 사용자의 정보가 포함된 CustomUserDetails 객체 (토큰에서 추출된 사용자 정보)
     * @return ApiResponse<Void> 나가기 완료 응답
     */
    @PatchMapping("/{groupId}/members/delete")
    public ApiResponse<Void> leaveGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Long memberId = userDetails.getId();
        groupService.leaveGroup(groupId, memberId);

        return ApiResponse.of(HttpStatus.NO_CONTENT, null);

    }
}
