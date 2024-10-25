package com.codeZero.photoMap.service.group;

import com.codeZero.photoMap.common.exception.GroupNotFoundException;
import com.codeZero.photoMap.common.exception.MemberNotFoundException;
import com.codeZero.photoMap.domain.group.MemberGroup;
import com.codeZero.photoMap.domain.group.MemberGroupMapping;
import com.codeZero.photoMap.domain.group.MemberGroupMappingRepository;
import com.codeZero.photoMap.domain.group.MemberGroupRepository;
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
     * @return
     */
    @Transactional
    public GroupResponse createGroup(GroupCreateRequest request, Long memberId) {
        MemberGroup group = MemberGroup.builder()
                .groupName(request.getGroupName())
                .build();

        MemberGroup savedGroup = memberGroupRepository.save(group);

        //그룹을 생성한 멤버 추가
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member를 찾을 수 없습니다."));
        savedGroup.addMember(member);

        //멤버 리스트 조회
        List<Member> members = memberGroupMappingRepository.findMembersByGroupId(savedGroup.getId());

        return GroupResponse.from(savedGroup, members);
    }

    /**
     * 그룹 조회(로그인한 멤버가 가진 그룹 조회)
     * @param memberId 로그인한 멤버Id
     * @return
     */
    @Transactional(readOnly = true)
    public List<GroupResponse> getGroupByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member를 찾을 수 없습니다."));

        //로그인한 멤버가 속한 그룹들을 조회 -> 각 그룹의 멤버들 조회. ...
        return member.getGroups().stream()
                .map(MemberGroupMapping::getMemberGroup)
                .map(group -> {
                    // 각 그룹의 멤버를 조회
                    List<Member> members = memberGroupMappingRepository.findMembersByGroupId(group.getId());
                    // GroupResponse로 변환 (그룹과 멤버 리스트를 함께 전달)
                    return GroupResponse.from(group, members);
                })
                .collect(Collectors.toList());
    }

    /**
     * 그룹 조회(하나의 그룹이 가진 그룹정보,멤버 조회)
     * @param groupId 조회할 그룹Id
     * @return
     */
    @Transactional(readOnly = true)
    public GroupResponse getGroup(Long groupId) {
        //그룹 정보 조회
        MemberGroup group = memberGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group을 찾을 수 없습니다."));
        //그룹에 속한 멤버 조회
        List<Member> members = memberGroupMappingRepository.findMembersByGroupId(groupId);

        return GroupResponse.from(group, members);
    }

    /**
     * 그룹 이름 수정
     * @param groupId 수정할 그룹Id
     * @param request 그룹 수정 요청 DTO
     * @return
     */
    @Transactional
    public GroupResponse updateGroup(Long groupId, GroupUpdateRequest request) {
        MemberGroup group = memberGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group을 찾을 수 없습니다."));

        group.updateGroupName(request.getGroupName());
        MemberGroup updatedGroup = memberGroupRepository.save(group);

        //멤버 리스트 조회
        List<Member> members = memberGroupMappingRepository.findMembersByGroupId(updatedGroup.getId());

        return GroupResponse.from(updatedGroup, members);
    }

}
