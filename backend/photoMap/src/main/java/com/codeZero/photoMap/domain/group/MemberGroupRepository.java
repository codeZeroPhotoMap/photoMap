package com.codeZero.photoMap.domain.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberGroupRepository extends JpaRepository<MemberGroup, Long> {

    Optional<MemberGroup> findByIdAndIsDeletedFalse(Long groupId);

}
