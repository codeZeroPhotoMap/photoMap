package com.codeZero.photoMap.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    //이메일로 전체 멤버 조회
    Optional<Member> findByEmail(String email);

    //이메일이 존재하는지 확인(소프트 딜리트 되지 않은 멤버의 이메일)
    boolean existsByEmailAndIsDeletedFalse(String email);

    //이메일로 멤버를 조회(소프트 딜리트 되지 않은 멤버 조회)
    Optional<Member> findByEmailAndIsDeletedFalse(String email);

    // 소프트 딜리트가 안 된 멤버만 조회
    List<Member> findByIsDeletedFalse();

    // ID로 특정 멤버 조회할 때도 소프트 딜리트가 안 된 멤버만 조회
    Optional<Member> findByIdAndIsDeletedFalse(Long memberId);
}
