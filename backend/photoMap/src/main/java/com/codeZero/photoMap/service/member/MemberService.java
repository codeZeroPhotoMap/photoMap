package com.codeZero.photoMap.service.member;

import com.codeZero.photoMap.domain.member.Member;
import com.codeZero.photoMap.domain.member.MemberRepository;
import org.springframework.stereotype.Service;


@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * 멤버 생성 테스트 용
     * @param email
     * @param password
     * @param name
     * @return
     */
    public Member createMember(String email, String password, String name) {
        Member member = Member.builder()
                .email(email)
                .password(password)
                .name(name)
                .build();

        return memberRepository.save(member);
    }
}

