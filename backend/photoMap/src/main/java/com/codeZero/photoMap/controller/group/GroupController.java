package com.codeZero.photoMap.controller.group;

import com.codeZero.photoMap.dto.group.request.GroupCreateRequest;
import com.codeZero.photoMap.dto.group.request.GroupUpdateRequest;
import com.codeZero.photoMap.dto.group.response.GroupResponse;
import com.codeZero.photoMap.service.group.GroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     *
     * @param request 그룹 생성 요청 DTO
     * @return
     */
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @RequestBody GroupCreateRequest request,
            @RequestParam Long memberId
    ) {
        GroupResponse response = groupService.createGroup(request, memberId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 그룹 조회 API(로그인한 멤버가 가진 그룹 조회)
     * @param memberId 로그인한 멤버Id
     * @return
     */
    @GetMapping
    public ResponseEntity<List<GroupResponse>> getGroupByMember(
            @RequestParam Long memberId
    ) {
        List<GroupResponse> responses = groupService.getGroupByMember(memberId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 그룹 조회 API(하나의 그룹이 가진 그룹정보,멤버 조회)
     *
     * @param groupId 조회할 그룹Id
     * @return
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(
            @PathVariable Long groupId
    ) {
        GroupResponse response = groupService.getGroup(groupId);

        return ResponseEntity.ok(response);
    }

    /**
     * 그룹 수정
     *
     * @param groupId 수정할 그룹Id
     * @param request 그룹 수정 요청 DTO
     * @return
     */
    @PatchMapping("/{groupId}")
    public ResponseEntity<GroupResponse> updateGroup(
            @PathVariable Long groupId,
            @RequestBody GroupUpdateRequest request
    ) {
        GroupResponse response = groupService.updateGroup(groupId, request);

        return ResponseEntity.ok(response);

    }

//    /**
//     * 그룹에 멤버 추가 (아직)
//     *
//     * @param groupId 멤버를 추가할 그룹Id
//     * @param memberId 추가할 멤버Id
//     * @return
//     */
//    @PostMapping("/{groupId}/members/{memberId}")
//    public ResponseEntity<Void> addMemberToGroup(
//            @PathVariable Long groupId,
//            @PathVariable Long memberId
//    ) {
//        groupService.addMemberToGroup(groupId, memberId);
//        return ResponseEntity.status(HttpStatus.CREATED).build();
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
//    public ResponseEntity<Void> removeMemberFromGroup(
//            @PathVariable Long groupId,
//            @PathVariable Long memberId
//    ) {
//        groupService.removeMemberFromGroup(groupId, memberId);
//        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
//    }
}
