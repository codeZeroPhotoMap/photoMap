package com.codeZero.photoMap.service.group;

import com.codeZero.photoMap.common.exception.ForbiddenException;
import com.codeZero.photoMap.common.exception.NotFoundException;
import com.codeZero.photoMap.domain.group.*;
import com.codeZero.photoMap.domain.member.Member;
import com.codeZero.photoMap.domain.member.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class GroupInvitationService {

    private final GroupInvitationRepository groupInvitationRepository;
    private final MemberGroupRepository memberGroupRepository;
    private final MemberRepository memberRepository;
    private final MemberGroupMappingRepository memberGroupMappingRepository;

    public GroupInvitationService(GroupInvitationRepository groupInvitationRepository, MemberGroupRepository memberGroupRepository, MemberRepository memberRepository, MemberGroupMappingRepository memberGroupMappingRepository) {
        this.groupInvitationRepository = groupInvitationRepository;
        this.memberGroupRepository = memberGroupRepository;
        this.memberRepository = memberRepository;
        this.memberGroupMappingRepository = memberGroupMappingRepository;
    }

    /**
     * 초대 수락(그룹에 멤버 추가)
     * @param token 초대 토큰
     * @param memberId  로그인한 멤버Id
     */
    @Transactional
    public void acceptInvitation(String token, Long memberId) {
        GroupInvitation invitation = groupInvitationRepository.findByTokenAndIsDeletedFalse(token)
                .orElseThrow(() -> new NotFoundException("유효하지 않거나 만료된 초대 토큰입니다."));

        if (invitation.isUsed()) {
            throw new IllegalArgumentException("이미 사용된 초대입니다.");
        }

        if (invitation.isExpired()) {
            throw new IllegalArgumentException("초대 토큰이 만료되었습니다.");
        }

        //그룹, 멤버 확인
        MemberGroup group = memberGroupRepository.findByIdAndIsDeletedFalse(invitation.getGroupId())
                .orElseThrow(() -> new NotFoundException("그룹을 찾을 수 없습니다."));
        Member member = memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new NotFoundException("멤버를 찾을 수 없습니다."));

        //이미 그룹에 속한 멤버인지 확인
        if (memberGroupMappingRepository.existsByMemberGroupAndMemberAndIsDeletedFalse(group, member)) {
            throw new IllegalArgumentException("이미 그룹에 속해 있는 멤버입니다.");
        }

        //멤버가 이미 그룹에 추가된 이력이 있는지 확인
        if (memberGroupMappingRepository.existsByMemberGroupIdAndMemberIdAndIsDeletedFalse(invitation.getGroupId(), memberId)) {
            throw new IllegalArgumentException("이미 이 그룹에 참여하거나 거절한 기록이 있습니다.");
        }

        //초대된 이메일과 로그인된 사용자의 이메일이 일치하는지 확인
        if (!invitation.getEmail().equals(member.getEmail())) {
            throw new ForbiddenException("이 초대는 현재 로그인된 사용자의 초대가 아닙니다.");
        }

        //초대 토큰 사용으로 변경
        invitation.use();
        groupInvitationRepository.save(invitation);

        //빌더 패턴으로 객체 생성 및 저장
        MemberGroupMapping mapping = MemberGroupMapping.builder()
                .member(member)
                .memberGroup(group)
                .role(Role.MEMBER)
                .build();
        memberGroupMappingRepository.save(mapping);

    }

}

