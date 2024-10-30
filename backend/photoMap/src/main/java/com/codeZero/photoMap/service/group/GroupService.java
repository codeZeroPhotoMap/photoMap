package com.codeZero.photoMap.service.group;

import com.codeZero.photoMap.common.exception.NotFoundException;
import com.codeZero.photoMap.domain.group.*;
import com.codeZero.photoMap.domain.member.Member;
import com.codeZero.photoMap.domain.member.MemberRepository;
import com.codeZero.photoMap.dto.group.request.GroupCreateRequest;
import com.codeZero.photoMap.dto.group.request.GroupUpdateRequest;
import com.codeZero.photoMap.dto.group.response.GroupResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final MemberGroupRepository memberGroupRepository;
    private final MemberRepository memberRepository;
    private final MemberGroupMappingRepository memberGroupMappingRepository;

    public GroupService(MemberGroupRepository memberGroupRepository, MemberRepository memberRepository, MemberGroupMappingRepository memberGroupMappingRepository) {
        this.memberGroupRepository = memberGroupRepository;
        this.memberRepository = memberRepository;
        this.memberGroupMappingRepository = memberGroupMappingRepository;
    }


    /**
     * 그룹 생성
     * @param request 그룹 생성 요청 DTO
     * @param memberId 그룹을 생성하는 멤버Id
     * @return GroupResponse 생성된 그룹과 생성한 멤버 응답 DTO
     */
    @Transactional
    public GroupResponse createGroup(GroupCreateRequest request, Long memberId) {
        MemberGroup group = MemberGroup.builder()
                .groupName(request.getGroupName())
                .build();

        MemberGroup savedGroup = memberGroupRepository.save(group);

        //그룹을 생성한 멤버 추가
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member를 찾을 수 없습니다."));
        savedGroup.addMemberWithRole(member, Role.ADMIN);   //생성한 멤버에 ADIMIN역할 추가

        return GroupResponse.from(savedGroup, List.of(member));
    }

    /**
     * 그룹 조회(로그인한 멤버가 가진 그룹 조회)
     * @param memberId 로그인한 멤버Id
     * @return ApiResponse<List<GroupResponse>> 로그인한 멤버가 속한 모든 그룹, 각 그룹의 멤버 목록 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<GroupResponse> getGroupByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member를 찾을 수 없습니다."));

        //로그인한 멤버가 속한 그룹들을 조회 -> 각 그룹의 멤버들 조회. ...
        return member.getGroups().stream()
                .map(MemberGroupMapping::getMemberGroup)
                .filter(group -> !group.isDeleted()) //소프트 삭제된 그룹 필터링
                .map(group -> {
                    //각 그룹의 멤버들 중 소프트 삭제되지 않은 멤버들 조회
                    List<Member> members = memberGroupMappingRepository.findByMemberGroupIdAndIsDeletedFalse(group.getId())
                            .stream()
                            .map(MemberGroupMapping::getMember)
                            .collect(Collectors.toList());

                    //GroupResponse로 변환 (그룹과 멤버 리스트를 함께 전달)
                    return GroupResponse.from(group, members);
                })
                .collect(Collectors.toList());
    }

    /**
     * 그룹 조회(하나의 그룹이 가진 그룹정보,멤버 조회)
     * @param groupId 조회할 그룹Id
     * @return ApiResponse<GroupResponse> 그룹 정보와 멤버 목록 응답 DTO
     */
    @Transactional(readOnly = true)
    public GroupResponse getGroup(Long groupId) {
        //그룹 정보 조회
        MemberGroup group = memberGroupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new NotFoundException("Group을 찾을 수 없습니다."));
        //그룹에 속한 멤버 조회
        List<Member> members = memberGroupMappingRepository.findMembersByGroupIdIsDeletedFalse(groupId);

        return GroupResponse.from(group, members);
    }

    /**
     * 그룹 이름 수정
     * @param groupId 수정할 그룹Id
     * @param request 그룹 수정 요청 DTO
     * @return GroupResponse 수정된 그룹과 해당 그룹의 멤버 목록 응답 DTO
     */
    @Transactional
    public GroupResponse updateGroup(Long groupId, GroupUpdateRequest request) {
        MemberGroup group = memberGroupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new NotFoundException("Group을 찾을 수 없습니다."));

        group.updateGroupName(request.getGroupName());
        MemberGroup updatedGroup = memberGroupRepository.save(group);

        //멤버 리스트 조회
        List<Member> members = memberGroupMappingRepository.findMembersByGroupIdIsDeletedFalse(updatedGroup.getId());

        return GroupResponse.from(updatedGroup, members);
    }

    /**
     * 그룹 삭제
     * @param groupId 삭제할 그룹Id
     * @return GroupResponse 소프트 삭제된 그룹 정보 응답 DTO
     */
    @Transactional
    public GroupResponse deleteGroup(Long groupId) {

        MemberGroup group = memberGroupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new NotFoundException("Group을 찾을 수 없습니다."));

        group.delete();

        // 그룹에 속한 모든 MemberGroupMapping도 소프트 삭제 처리
        List<MemberGroupMapping> mappings = memberGroupMappingRepository.findByMemberGroupIdAndIsDeletedFalse(groupId);
        for (MemberGroupMapping mapping : mappings) {
            mapping.delete(); // 각 mapping에 대한 delete() 메서드 호출
            memberGroupMappingRepository.save(mapping); // 소프트 삭제된 mapping 저장
        }

        MemberGroup deletedGroup = memberGroupRepository.save(group);

        return GroupResponse.of(deletedGroup);
    }

}
