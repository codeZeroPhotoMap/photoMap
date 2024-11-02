package com.codeZero.photoMap.controller.group;

import com.codeZero.photoMap.common.ApiResponse;
import com.codeZero.photoMap.dto.group.request.GroupCreateRequest;
import com.codeZero.photoMap.dto.group.request.GroupUpdateRequest;
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
     * @param request
     * @param userDetails 인증된 사용자의 정보가 포함된 객체(토큰에서 추출된 로그인한 사용자 정보)
     * @return ApiResponse<GroupResponse> 생성된 그룹 응답 DTO
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
     * @param userDetails 인증된 사용자의 정보가 포함된 객체(토큰에서 추출된 로그인한 사용자 정보)
     * @return ApiResponse<List<GroupResponse>> 로그인한 멤버가 속한 모든 그룹 응답 DTO 목록
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
     *
     * @param groupId 조회할 그룹Id
     * @return ApiResponse<GroupResponse> 요청 그룹의 정보와 멤버 목록 응답 DTO
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
     *
     * @param groupId 수정할 그룹Id
     * @param request 그룹 수정 요청 DTO
     * @return ApiResponse<GroupResponse> 수정된 그룹 정보 응답 DTO
     */
    @PatchMapping("/{groupId}")
    public ApiResponse<GroupResponse> updateGroup(
            @PathVariable Long groupId,
            @RequestBody GroupUpdateRequest request
    ) {
        GroupResponse response = groupService.updateGroup(groupId, request);

        return ApiResponse.ok(response);

    }

    /**
     * 그룹 소프트 삭제 API
     * @param groupId 삭제할 그룹 ID
     * @return ApiResponse<GroupResponse> 삭제된 그룹 정보 응답 DTO(HTTPStatus 204)
     */
    @PatchMapping("/{groupId}/delete")
    public ApiResponse<GroupResponse> deleteGroup(@PathVariable Long groupId) {
        GroupResponse response = groupService.deleteGroup(groupId);
        return ApiResponse.of(HttpStatus.NO_CONTENT, response);
    }

//    /**
//     * 그룹에 멤버 추가 (아직)
//     *
//     * @param groupId 멤버를 추가할 그룹Id
//     * @param memberId 추가할 멤버Id
//     * @return
//     */
//    @PostMapping("/{groupId}/members/{memberId}")
//    public ApiResponse<Void> addMemberToGroup(
//            @PathVariable Long groupId,
//            @PathVariable Long memberId
//    ) {
//        groupService.addMemberToGroup(groupId, memberId);
//        return ApiResponse.of(HttpStatus.CREATED,null);
//    }
//
//    /**
//     * 그룹에서 멤버 삭제 (아직)
//     *
//     * @param groupId 멤버를 삭제할 그룹Id
//     * @param memberId 삭제할 멤버Id
//     * @return
//     */
//    @DeleteMapping("/{groupId}/members/{memberId}")
//    public ApiResponse<Void> removeMemberFromGroup(
//            @PathVariable Long groupId,
//            @PathVariable Long memberId
//    ) {
//        groupService.removeMemberFromGroup(groupId, memberId);
//        return ApiResponse.of(HttpStatus.NO_CONTENT,null);
//    }
}
