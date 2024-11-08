package com.codeZero.photoMap.domain.photo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    Optional<Photo> findByIdAndIsDeletedFalse(Long photoId);

    Optional<Photo> findByIdAndIsDeletedFalseAndUploadStatusTrue(Long photoId);

    List<Photo> findByMemberIdAndIsDeletedFalseAndUploadStatusTrue(Long memberId);

    List<Photo> findByLocationIdAndIsDeletedFalseAndUploadStatusTrue(Long locationId);
}
