package com.codeZero.photoMap.service.location;

import com.codeZero.photoMap.common.exception.DuplicateException;
import com.codeZero.photoMap.common.exception.ForbiddenException;
import com.codeZero.photoMap.common.exception.NotFoundException;
import com.codeZero.photoMap.domain.group.MemberGroup;
import com.codeZero.photoMap.domain.group.MemberGroupMappingRepository;
import com.codeZero.photoMap.domain.group.MemberGroupRepository;
import com.codeZero.photoMap.domain.location.Location;
import com.codeZero.photoMap.domain.location.LocationRepository;
import com.codeZero.photoMap.dto.location.response.LocationResponse;
import com.codeZero.photoMap.dto.location.request.LocationServiceRequest;
import com.codeZero.photoMap.dto.location.request.LocationUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;
    private final MemberGroupRepository memberGroupRepository;
    private final MemberGroupMappingRepository memberGroupMappingRepository;

    /**
     * 위치 생성
     * @param memberId 멤버 ID
     * @param groupId 그룹 ID
     * @param request 위치 생성 요청 DTO
     * @return LocationResponse 생성된 위치 응답 DTO
     */
    public LocationResponse createLocation(Long memberId, Long groupId, LocationServiceRequest request) {

        // 해당 member가 실제로 group에 속해있는지 검증
        validateMemberInGroup(memberId, groupId);

        // 그룹 내 동일한 위치에 폴더가 있을 경우 예외 처리
        checkDuplicateLocationInGroup(groupId, request);

        MemberGroup memberGroup = memberGroupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new NotFoundException("해당 그룹을 찾을 수 없습니다."));

        Location location = request.toEntity(memberGroup);
        Location savedLocation = locationRepository.save(location);

        return LocationResponse.of(savedLocation);
    }

    private void checkDuplicateLocationInGroup(Long groupId, LocationServiceRequest request) {
        Optional<Location> existingLocation = locationRepository.findByMemberGroupIdAndLatitudeAndLongitudeAndIsDeletedFalse(groupId, request.getLatitude(), request.getLongitude());

        if (existingLocation.isPresent()) {
            throw new DuplicateException("이미 동일한 위치에 폴더가 존재합니다.");
        }
    }

    /**
     * 사용자 ID로 위치 리스트 조회
     * @param memberId 사용자 ID
     * @return List<LocationResponse> 위치 응답 DTO 리스트
     */
    public List<LocationResponse> getLocationsByMemberId(Long memberId) {

        // memberId로 해당 멤버가 속한 그룹들의 ID를 조회
        List<Long> groupIds = memberGroupMappingRepository.findGroupIdsByMemberIdAndIsDeletedFalse(memberId);
        if (groupIds.isEmpty()) {
            throw new NotFoundException("해당 멤버가 속한 그룹을 찾을 수 없습니다.");
        }

        // 그룹 ID들로 해당 그룹들에 속한 Location 조회
        List<Location> locations = locationRepository.findByMemberGroupIdInAndIsDeletedFalse(groupIds);

        // Location을 LocationResponse로 매핑하여 리스트로 변환
        return convertLocationsToResponses(locations);
    }

    /**
     * 사용자 ID와 그룹 ID로 그룹 별 위치 리스트 조회
     * @param memberId 사용자 ID
     * @param groupId 그룹 ID
     * @return List<LocationResponse> 위치 응답 DTO 리스트
     */
    public List<LocationResponse> getLocationsByGroupId(Long memberId, Long groupId) {

        // 해당 member가 실제로 group에 속해있는지 검증
        validateMemberInGroup(memberId, groupId);

        // 그룹 ID로 해당 그룹에 속한 Location 조회
        List<Location> locations = locationRepository.findByMemberGroupIdAndIsDeletedFalse(groupId);

        // Location을 LocationResponse로 매핑하여 리스트로 변환
        return convertLocationsToResponses(locations);
    }

    /**
     * Location 이름, 위도, 경도 수정
     * @param locationId Location ID
     * @param request 위치 수정 요청 DTO (이름, 위도, 경도 값)
     * @return LocationResponse 수정된 위치 응답 DTO
     */
    public LocationResponse updateLocation(Long locationId, LocationUpdateRequest request) {
        Location location = locationRepository.findByIdAndIsDeletedFalse(locationId)
                .orElseThrow(() -> new NotFoundException("해당 Location을 찾을 수 없습니다."));

        location.updateLocation(request.getName(), request.getLatitude(), request.getLongitude());
        Location updatedLocation = locationRepository.save(location);

        return LocationResponse.of(updatedLocation);
    }

    /**
     * Location 삭제
     * @param locationId Location ID
     */
    public LocationResponse deleteLocation(Long locationId) {
        Location location = locationRepository.findByIdAndIsDeletedFalse(locationId)
                .orElseThrow(() -> new NotFoundException("해당 Location을 찾을 수 없습니다."));

        location.delete();

        Location deletedLocation = locationRepository.save(location);

        return LocationResponse.of(deletedLocation);
    }

    /**
     * 해당 member가 실제로 group에 속해있는지 검증하는 메서드
     * @param memberId 멤버 ID
     * @param groupId 그룹 ID
     */
    private void validateMemberInGroup(Long memberId, Long groupId) {
        List<Long> groupIds = memberGroupMappingRepository.findGroupIdsByMemberIdAndIsDeletedFalse(memberId);
        if (!groupIds.contains(groupId)) {
            throw new ForbiddenException("해당 사용자가 속해 있는 그룹이 아닙니다.");
        }
    }

    /**
     * Location 리스트를 LocationResponse 리스트로 변환
     * @param locations
     * @return
     */
    private List<LocationResponse> convertLocationsToResponses(List<Location> locations) {
        return locations.stream()
                .map(LocationResponse::of)
                .collect(Collectors.toList());
    }
}
