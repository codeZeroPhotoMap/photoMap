package com.codeZero.photoMap.domain.group;

import com.codeZero.photoMap.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberGroupMappingRepository extends JpaRepository<MemberGroupMapping, Long> {

    //  특정 그룹에 속한 소프트 삭제되지 않은 MemberGroupMapping 엔티티 조회
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
