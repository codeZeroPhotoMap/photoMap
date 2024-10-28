package com.codeZero.photoMap.domain.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByIdAndIsDeletedFalse(Long locationId);

    Optional<Location> findByLatitudeAndLongitudeAndIsDeletedFalse(double latitude, double longitude);

    List<Location> findByGroupIdAndIsDeletedFalse(Long groupId);

    List<Location> findByGroupIdInAndIsDeletedFalse(List<Long> groupIds);

}
