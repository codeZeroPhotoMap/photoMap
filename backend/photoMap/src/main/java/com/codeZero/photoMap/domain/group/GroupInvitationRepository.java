package com.codeZero.photoMap.domain.group;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {

    //특정 이메일과 그룹 ID에 대한 초대가 존재하며 이미 사용된(수락 또는 거절된) 상태인지 확인
    boolean existsByEmailAndGroupIdAndIsUsedTrue(String email, Long groupId);

    //주어진 토큰과 소프트 삭제되지 않은 상태의 초대를 조회
    Optional<GroupInvitation> findByGroupTokenAndIsDeletedFalse(String groupToken);

    //주어진 ID와 소프트 삭제되지 않은 상태의 초대를 조회
    Optional<GroupInvitation> findByIdAndIsDeletedFalse(Long id);
}
