package com.codeZero.photoMap.security;

import com.codeZero.photoMap.common.exception.NotFoundException;
import com.codeZero.photoMap.domain.member.Member;
import com.codeZero.photoMap.domain.member.MemberRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws NotFoundException {
        Member member = memberRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다."));

        //MemberRole을 GrantedAuthority로 변환
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(member.getRole().name()));

        return new CustomUserDetails(member.getId(), member.getEmail(), member.getPassword(), authorities);
    }

}
