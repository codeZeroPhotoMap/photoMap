package com.codeZero.photoMap.domain.group;

import com.codeZero.photoMap.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberGroupMappingRepository extends JpaRepository<MemberGroupMapping, Long> {

    // 특정 그룹에 속한 멤버들을 조회하는 쿼리 ...
    @Query("SELECT mgm.member FROM MemberGroupMapping mgm WHERE mgm.memberGroup.id = :groupId")
    List<Member> findMembersByGroupId(@Param("groupId") Long groupId);

}
