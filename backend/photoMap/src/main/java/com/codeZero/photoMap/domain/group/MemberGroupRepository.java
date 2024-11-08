package com.codeZero.photoMap.domain.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberGroupRepository extends JpaRepository<MemberGroup, Long> {

    //특정 그룹 ID를 기반으로 소프트 딜리트되지 않은 그룹을 조회
    Optional<MemberGroup> findByIdAndIsDeletedFalse(Long groupId);

    //특정 멤버가 그룹장으로 있는 소프트 딜리트되지 않은 그룹들을 조회
    List<MemberGroup> findByOwnerIdAndIsDeletedFalse(Long ownerId);

}
