package com.codeZero.photoMap.service.group;

import com.codeZero.photoMap.common.exception.DuplicateException;
import com.codeZero.photoMap.common.exception.ForbiddenException;
import com.codeZero.photoMap.common.exception.NotFoundException;
import com.codeZero.photoMap.common.service.EmailService;
import com.codeZero.photoMap.domain.group.*;
import com.codeZero.photoMap.domain.member.Member;
import com.codeZero.photoMap.domain.member.MemberRepository;
import com.codeZero.photoMap.dto.group.request.GroupCreateRequest;
import com.codeZero.photoMap.dto.group.request.GroupUpdateRequest;
import com.codeZero.photoMap.dto.group.response.GroupResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final MemberGroupRepository memberGroupRepository;
    private final MemberRepository memberRepository;
    private final MemberGroupMappingRepository memberGroupMappingRepository;
    private final EmailService emailService;
    private final GroupInvitationRepository groupInvitationRepository;

    public GroupService(MemberGroupRepository memberGroupRepository, MemberRepository memberRepository, MemberGroupMappingRepository memberGroupMappingRepository, EmailService emailService, GroupInvitationRepository groupInvitationRepository) {
        this.memberGroupRepository = memberGroupRepository;
        this.memberRepository = memberRepository;
        this.memberGroupMappingRepository = memberGroupMappingRepository;
        this.emailService = emailService;
        this.groupInvitationRepository = groupInvitationRepository;
    }


    /**
     * 그룹 생성
     * @param request   그룹 생성 요청 DTO
     * @param memberId  그룹을 생성하는 멤버Id
     * @return GroupResponse    생성된 그룹과 생성한 멤버 응답 DTO
     */
    @Transactional
    public GroupResponse createGroup(GroupCreateRequest request, Long memberId) {
        MemberGroup group = MemberGroup.builder()
                .groupName(request.getGroupName())
                .ownerId(memberId)
                .build();

        MemberGroup savedGroup = memberGroupRepository.save(group);

        //그룹을 생성한 멤버 추가
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member를 찾을 수 없습니다."));
        savedGroup.addMemberWithRole(member, Role.OWNER);   //생성한 멤버에 ADIMIN역할(OWNER) 추가

        return GroupResponse.from(savedGroup, List.of(member));
    }

    /**
     * 그룹 조회(로그인한 멤버가 가진 그룹 조회)
     * @param memberId  로그인한 멤버Id
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
     * @param groupId   조회할 그룹Id
     * @return ApiResponse<GroupResponse>   그룹 정보와 멤버 목록 응답 DTO
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
     * @param groupId   수정할 그룹Id
     * @param request   그룹 수정 요청 DTO
     * @return GroupResponse    수정된 그룹과 해당 그룹의 멤버 목록 응답 DTO
     */
    @Transactional
    public GroupResponse updateGroup(Long groupId, Long memberId, GroupUpdateRequest request) {
        MemberGroup group = memberGroupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new NotFoundException("Group을 찾을 수 없습니다."));

        //그룹장만 이름 수정(검증)
        verifyGroupOwner(groupId, memberId);

        group.updateGroupName(request.getGroupName());
        MemberGroup updatedGroup = memberGroupRepository.save(group);

        //멤버 리스트 조회
        List<Member> members = memberGroupMappingRepository.findMembersByGroupIdIsDeletedFalse(updatedGroup.getId());

        return GroupResponse.from(updatedGroup, members);
    }

    /**
     * 그룹 삭제
     * @param groupId   삭제할 그룹Id
     */
    @Transactional
    public void deleteGroup(Long groupId, Long ownerId) {

        //그룹 확인 및 존재 여부 검증
        MemberGroup group = memberGroupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new NotFoundException("Group을 찾을 수 없습니다."));

        //그룹장 권한 확인
        if (!group.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("그룹장만 그룹을 삭제할 수 있습니다.");
        }

        //나만의 그룹(isPersonal이 true)일 경우 삭제 방지
        if (group.isPersonal()) {
            throw new ForbiddenException("나만의 그룹은 삭제할 수 없습니다.");
        }

        //그룹 소프트 딜리트
        group.delete();

        //그룹에 속한 모든 MemberGroupMapping도 소프트 삭제 처리
        List<MemberGroupMapping> mappings = memberGroupMappingRepository.findByMemberGroupIdAndIsDeletedFalse(groupId);
        for (MemberGroupMapping mapping : mappings) {
            mapping.delete(); //각 mapping에 대한 delete() 메서드 호출
            memberGroupMappingRepository.save(mapping); //소프트 삭제된 mapping 저장
        }

        //소프트 딜리트된 그룹 저장
        memberGroupRepository.save(group);
    }

    /**
     * 그룹에 멤버 초대URL 발송
     * @param email     초대받는 사용자의 이메일
     * @param groupId   초대할 그룹의 ID
     * @param memberId  로그인한 사용자의 ID (초대를 생성하려는 사용자)
     * @return 생성된 초대 토큰
     */
    @Transactional
    public String createInvitation(String email, Long groupId, Long memberId) {
        MemberGroup group = memberGroupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 그룹ID입니다."));

        //현재 로그인한 사용자가 그룹의 소유자인지 확인
        if (!group.getOwnerId().equals(memberId)) {
            throw new ForbiddenException("그룹장만 멤버를 초대할 수 있습니다.");
        }

        //나만의 그룹인지 확인(나만의 그룹에는 멤버 초대 불가)
        if (group.isPersonal()){
            throw new ForbiddenException("나만의 그룹에는 멤버를 초대할 수 없습니다.");
        }

        //이미 그룹에 초대된 멤버인지 확인
        if (memberGroupMappingRepository.existsByMemberGroupIdAndMemberEmailAndIsDeletedFalse(groupId, email)) {
            throw new DuplicateException("이미 초대된 멤버입니다.");
        }

        //고유 초대 토큰 생성 및 초대 객체 빌더 사용
        String groupToken = UUID.randomUUID().toString();
        GroupInvitation invitation = GroupInvitation.builder()
                .groupToken(groupToken)
                .email(email)
                .groupId(groupId)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        groupInvitationRepository.save(invitation);

        //그룹 이름 가져오기
        String groupName = group.getGroupName();
        //그룹 이름이 한글이름, 특수문자가 있을 수 있기 때문에 인코딩해서 파라미터로 전달
        String encodedGroupName = URLEncoder.encode(groupName, StandardCharsets.UTF_8);

        //수락 링크 생성
//        String acceptLink = "http://localhost:8080/api/invitations/accept?groupToken=" + groupToken + "&groupName=" + encodedGroupName;
//        String acceptLink = "http://52.79.152.88:8080/api/invitations/accept?groupToken=" + groupToken + "&groupName=" + encodedGroupName;
        String acceptLink = "https://goormfinal.vercel.app/invitation?groupToken=" + groupToken + "&groupName=" + encodedGroupName;

        //초대하는 멤버의 이름(닉네임) 가져오기
        Member inviter = memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new NotFoundException("초대하는 멤버를 찾을 수 없습니다."));
        String inviterName = inviter.getName();

        //초대 토큰 만료 시간 가져오기
        LocalDateTime expiryDate = invitation.getExpiryDate();

        //초대 이메일 발송
        emailService.sendInvitationEmail(email, acceptLink, inviterName, groupName, expiryDate);

        return groupToken;
    }

    /**
     * 그룹에서 멤버 삭제
     * @param groupId   멤버를 삭제할 그룹Id
     * @param memberId  삭제할 멤버Id
     * @param ownerId   현재 로그인한 그룹장멤버Id
     */
    @Transactional
    public void removeMemberFromGroup(Long groupId, Long memberId, Long ownerId) {
        //그룹 확인 및 존재 여부 검증
        MemberGroup group = memberGroupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new NotFoundException("그룹을 찾을 수 없습니다."));

        //멤버 확인
        Member member = memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new NotFoundException("멤버를 찾을 수 없습니다."));

        //그룹장 권한 확인
        verifyGroupOwner(groupId, ownerId);

        //그룹장이 삭제 대상인지 확인
        if (group.getOwnerId().equals(memberId)) {
            throw new ForbiddenException("그룹장은 삭제할 수 없습니다.");
        }

        //멤버의 그룹 매핑을 소프트 딜리트 처리
        memberGroupMappingRepository.findByMemberGroupAndMemberAndIsDeletedFalse(group, member)
                .ifPresentOrElse(memberGroupMapping -> {
                    memberGroupMapping.delete();
                    memberGroupMappingRepository.save(memberGroupMapping);
                }, () -> {
                    throw new NotFoundException("멤버를 찾을 수 없습니다.");
                });
    }

    /**
     * 그룹에서 나가기
     * @param groupId   나가길 원하는 그룹Id
     * @param memberId  나가길 원하는 멤버Id
     */
    @Transactional
    public void leaveGroup(Long groupId, Long memberId) {
        //그룹 존재 확인
        MemberGroup group = memberGroupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new NotFoundException("그룹을 찾을 수 없습니다."));

        //멤버 확인
        Member member = memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new NotFoundException("멤버를 찾을 수 없습니다."));

        //멤버-그룹 매핑 존재 여부 확인
        MemberGroupMapping mapping = memberGroupMappingRepository.findByMemberGroupAndMemberAndIsDeletedFalse(group, member)
                .orElseThrow(() -> new NotFoundException("해당 멤버는 그룹에 속해 있지 않습니다."));

        //그룹장은 그룹을 나갈 수 없음
        if (group.getOwnerId().equals(memberId)) {
            throw new ForbiddenException("그룹장은 그룹을 나갈 수 없습니다.");
        }

        //매핑 소프트 딜리트
        mapping.delete();
        memberGroupMappingRepository.save(mapping);
    }

    /**
     *그룹 소유자 여부 검증
     * @param groupId   검증할 그룹의 ID
     * @param memberId  검증할 멤버의 ID
     */
    public void verifyGroupOwner(Long groupId, Long memberId) {
        MemberGroup group = memberGroupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new NotFoundException("그룹을 찾을 수 없습니다."));

        if (!group.getOwnerId().equals(memberId)) {
            throw new ForbiddenException("그룹장만 이 작업을 수행할 수 있습니다.");
        }
    }
}
