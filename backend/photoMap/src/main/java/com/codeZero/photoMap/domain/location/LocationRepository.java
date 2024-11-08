package com.codeZero.photoMap.domain.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByIdAndIsDeletedFalse(Long locationId);

    Optional<Location> findByMemberGroupIdAndLatitudeAndLongitudeAndIsDeletedFalse(Long groupId, double latitude, double longitude);

    List<Location> findByMemberGroupIdAndIsDeletedFalse(Long groupId);

    @Query("SELECT l FROM Location l WHERE l.memberGroup.id IN :groupIds AND l.isDeleted = false")
    List<Location> findByMemberGroupIdInAndIsDeletedFalse(@Param("groupIds") List<Long> groupIds);

}
