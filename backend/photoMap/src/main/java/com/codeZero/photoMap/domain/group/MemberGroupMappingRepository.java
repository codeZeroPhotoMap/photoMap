package com.codeZero.photoMap.domain.group;

import com.codeZero.photoMap.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberGroupMappingRepository extends JpaRepository<MemberGroupMapping, Long> {

    //특정 멤버가 멤버로 속해 있는 소프트 딜리트되지 않은 그룹 매핑들을 조회
    List<MemberGroupMapping> findByMemberIdAndIsDeletedFalse(Long memberId);

    //특정 그룹과 멤버 간의 소프트 삭제되지 않은 매핑 관계를 조회
    Optional<MemberGroupMapping> findByMemberGroupAndMemberAndIsDeletedFalse(MemberGroup memberGroup, Member member);

    //특정 그룹과 멤버에 대한 매핑이 존재하고 소프트 삭제되지 않은지를 확인(객체기반)
    boolean existsByMemberGroupAndMemberAndIsDeletedFalse(MemberGroup memberGroup, Member member);

    //특정 그룹과 멤버에 대한 매핑이 존재하고 소프트 삭제되지 않은지를 확인(Id기반)
    boolean existsByMemberGroupIdAndMemberIdAndIsDeletedFalse(Long groupId, Long memberId);

    //그룹 ID와 멤버 이메일을 기반으로, 소프트 삭제되지 않은 매핑이 존재하는지 확인하는 메서드
    boolean existsByMemberGroupIdAndMemberEmailAndIsDeletedFalse(Long memberGroupId, String memberEmail);

    //특정 그룹에 속한 소프트 삭제되지 않은 MemberGroupMapping 엔티티 조회
    List<MemberGroupMapping> findByMemberGroupIdAndIsDeletedFalse(Long groupId);

    //특정 그룹 ID를 기준으로 해당 그룹에 속한 Member 엔티티들 조회
    @Query("SELECT mgm.member FROM MemberGroupMapping mgm WHERE mgm.memberGroup.id = :groupId")
    List<Member> findMembersByGroupIdIsDeletedFalse(@Param("groupId") Long groupId);


    // memberId로 해당 멤버가 속한 그룹들의 ID를 조회
//    List<Long> findMemberGroupIdsByMemberIdAndIsDeletedFalse(Long memberId);
    @Query("SELECT m.memberGroup.id FROM MemberGroupMapping m WHERE m.member.id = :memberId AND m.isDeleted = false")
    List<Long> findGroupIdsByMemberIdAndIsDeletedFalse(@Param("memberId") Long memberId);
    /*
    @Query("SELECT mgm.memberGroup.id FROM MemberGroupMapping mgm WHERE mgm.member.id = :memberId")
    List<Long> findMemberGroupIdsByMemberIdAndIsDeletedFalse(@Param("memberId") Long memberId);
    */
}
