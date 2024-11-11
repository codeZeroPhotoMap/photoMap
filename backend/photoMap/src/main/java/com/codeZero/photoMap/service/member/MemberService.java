package com.codeZero.photoMap.service.member;

import com.codeZero.photoMap.common.exception.DuplicateException;
import com.codeZero.photoMap.common.exception.ForbiddenException;
import com.codeZero.photoMap.common.exception.NotFoundException;
import com.codeZero.photoMap.domain.group.*;
import com.codeZero.photoMap.domain.member.Member;
import com.codeZero.photoMap.domain.member.MemberRepository;
import com.codeZero.photoMap.domain.member.MemberRole;
import com.codeZero.photoMap.dto.member.request.LoginRequest;
import com.codeZero.photoMap.dto.member.request.MemberRequest;
import com.codeZero.photoMap.dto.member.request.NameUpdateRequest;
import com.codeZero.photoMap.dto.member.request.PasswordUpdateRequest;
import com.codeZero.photoMap.dto.member.response.MemberResponse;
import com.codeZero.photoMap.security.JwtTokenProvider;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberGroupRepository memberGroupRepository;
    private final MemberGroupMappingRepository memberGroupMappingRepository;


    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, MemberGroupRepository memberGroupRepository, MemberGroupMappingRepository memberGroupMappingRepository) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberGroupRepository = memberGroupRepository;
        this.memberGroupMappingRepository = memberGroupMappingRepository;
    }

    /**
     *  회원가입
     * @param request 회원가입 요청 DTO
     */
    public void registerMember(MemberRequest request) {
        //이메일 중복 확인
        if (memberRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new DuplicateException("이미 존재하는 이메일입니다");
        }

        //비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Member member = Member.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .role(MemberRole.ROLE_USER)
                .build();

        //회원 저장
        memberRepository.save(member);

        //나만의 그룹 생성
        createPersonalGroup(member);
    }

    /**
     * 이메일 중복 확인
     * @param email 이메일 주소
     * @return 중복여부
     */
    public boolean checkDuplicateEmail(String email) {
        return memberRepository.existsByEmailAndIsDeletedFalse(email);
    }

    /**
     * 나만의 그룹 생성
     * @param member 회원가입한 멤버 객체
     */
    private void createPersonalGroup(Member member) {

        MemberGroup personalGroup = MemberGroup.builder()
                .groupName(member.getName() + "님의 나만의 그룹")
                .isPersonal(true)
                .ownerId(member.getId())
                .build();

        personalGroup.addMemberWithRole(member, Role.OWNER);
        memberGroupRepository.save(personalGroup);
    }

    /**
     * 로그인
     * @param request 로그인 요청 DTO
     * @return JWT 토큰
     */
    public String login(LoginRequest request) {
        //이메일로 회원 조회
        Member member = memberRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다"));

        //비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new DuplicateException("비밀번호가 일치하지 않습니다");
        }

        //JWT 토큰 생성 후 반환
        return jwtTokenProvider.createToken(member.getEmail());  //JwtTokenProvider 를 통해 토큰 생성
    }

    /**
     * 카카오로 회원가입 & 로그인
     * @param email 카카오이메일
     * @param nickname  카카오닉네임
     * @return JWT 토큰
     */
    public String processKakaoLogin(String email, String nickname) {

        if (email == null || email.isEmpty()) {
            throw new NotFoundException("이메일 정보가 없습니다. 카카오 로그인 설정을 확인해주세요.");
        }

        //이메일로 멤버 찾기(소프트 딜리트된 멤버여도 찾기)
        Optional<Member> existingMember = memberRepository.findByEmail(email);

        //TODO : test용, 삭제예정
        System.out.println("member with email: " + email + " and nickname: " + nickname);


        if (existingMember.isPresent()) {

            //기존 회원인 경우
            Member member = existingMember.get();

            //탈퇴한 회원인지 확인
            if (member.isDeleted()) {
                throw new ForbiddenException("탈퇴한 회원입니다. 로그인이 불가합니다.");
            }

            return jwtTokenProvider.createToken(member.getEmail());

        } else{

            //TODO : 변경 예정
            //새로운 회원이라면 더미 비밀번호를 설정하여 DB에 저장
            String dummyPassword = "kakao_login_dummy_password";

            Member newMember = Member.builder()
                    .email(email)
                    .password(dummyPassword) //카카오 로그인 사용자를 위한 더미 비밀번호(비밀번호 필수이기 때문)
                    .name(nickname)
                    .role(MemberRole.ROLE_USER)
                    .build();

            memberRepository.save(newMember);

            //나만의 그룹 생성
            createPersonalGroup(newMember);

            return jwtTokenProvider.createToken(newMember.getEmail());

        }
    }

    /**
     * 로그인한 회원 정보 조회
     * @param email 멤버 email
     * @return MemberResponse 회원 응답 DTO
     */
    public MemberResponse getMemberInfo(String email) {

        Member member = memberRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다"));

        return MemberResponse.from(member);
    }

    /**
     * 이름 변경
     * @param memberId  변경할 멤버Id
     * @param request   이름 변경 요청 DTO
     * @return MemberResponse 회원 응답 DTO
     */
    public MemberResponse updateName(Long memberId, NameUpdateRequest request) {

        Member member = findMemberById(memberId);

        member.updateName(request.getName());

        memberRepository.save(member);

        return MemberResponse.from(member);
    }

    /**
     * 비밀번호 변경
     * @param memberId 변경할 멤버Id
     * @param request   비밀번호 요청 DTO
     * @return MemberResponse 회원 응답 DTO
     */
    public MemberResponse updatePassword(Long memberId, PasswordUpdateRequest request) {

        Member member = findMemberById(memberId);

        //기존 비밀번호 검증
        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        String encodePassword = passwordEncoder.encode(request.getNewPassword());

        member.updatePassword(encodePassword);

        memberRepository.save(member);

        return MemberResponse.from(member);
    }

    /**
     * 회원 탈퇴
     * @param memberId 탈퇴할 회원Id
     */
    public void deleteMember(Long memberId) {

        //멤버 조회, 소프트 딜리트
        Member member = findMemberById(memberId);
        member.delete();
        memberRepository.save(member);

        //그룹장으로 있는 그룹 소프트 딜리트 처리
        List<MemberGroup> ownedGroups = memberGroupRepository.findByOwnerIdAndIsDeletedFalse(memberId);
        for (MemberGroup group : ownedGroups) {
            group.delete();
            memberGroupRepository.save(group);

            //해당 그룹에 속해 있는 모든 멤버들의 그룹 매핑 소프트 딜리트 처리
            List<MemberGroupMapping> groupMappings = memberGroupMappingRepository.findByMemberGroupIdAndIsDeletedFalse(group.getId());
            for (MemberGroupMapping mapping : groupMappings) {
                mapping.delete();
                memberGroupMappingRepository.save(mapping);
            }
        }

        //멤버로 속해 있는 그룹에서 멤버 그룹 매핑 소프트 딜리트 처리
        List<MemberGroupMapping> memberMappings = memberGroupMappingRepository.findByMemberIdAndIsDeletedFalse(memberId);
        for (MemberGroupMapping mapping : memberMappings) {
            mapping.delete();
            memberGroupMappingRepository.save(mapping);
        }
    }

    /**
     * 회원 조회
     * @param memberId 조회할 멤버Id
     * @return Member 객체
     */
    private Member findMemberById(Long memberId) {

        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("회원이 존재하지 않습니다"));

    }

}

