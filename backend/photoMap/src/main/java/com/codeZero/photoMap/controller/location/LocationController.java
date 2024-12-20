package com.codeZero.photoMap.controller.location;

import com.codeZero.photoMap.common.ApiResponse;
import com.codeZero.photoMap.dto.location.request.LocationCreateRequest;
import com.codeZero.photoMap.dto.location.response.LocationResponse;
import com.codeZero.photoMap.dto.location.request.LocationUpdateRequest;
import com.codeZero.photoMap.security.CustomUserDetails;
import com.codeZero.photoMap.service.location.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    /**
     * 위치 생성 API
     * @param groupId 그룹 ID (PathVariable)
     * @param request 위치 생성 요청 DTO (name, latitude, longitude)
     * @return LocationResponse 생성된 위치 응답 DTO
     */
    @PostMapping("/groups/{groupId}")
    public ApiResponse<LocationResponse> createLocation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId,
            @RequestBody LocationCreateRequest request
    ) {
        LocationResponse location = locationService.createLocation(userDetails.getId(), groupId, request.toServiceRequest());

        return ApiResponse.ok(location);
    }

    /**
     * 사용자 ID로 위치 리스트 조회 API
     * @param userDetails JWT 토큰 정보 (memberId)
     * @return List<LocationResponse> 위치 응답 DTO 리스트
     */
    @GetMapping
    public ApiResponse<List<LocationResponse>> getLocationsByMemberId(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(locationService.getLocationsByMemberId(userDetails.getId()));
    }

    /**
     * 그룹 별 위치 리스트 조회 API
     * @param groupId 그룹 ID
     * @param userDetails JWT 토큰 정보 (memberId)
     * @return List<LocationResponse> 위치 응답 DTO 리스트
     */
    @GetMapping("/groups/{groupId}")
    public ApiResponse<List<LocationResponse>> getLocationsByGroupId(
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(locationService.getLocationsByGroupId(userDetails.getId(), groupId));
    }

    /**
     * 위치 이름, 위도, 경도 수정 API
     * @param locationId 위치 ID (PathVariable)
     * @param request 위치 수정 요청 DTO (name, latitude, longitude)
     * @return LocationResponse 위치 응답 DTO
     */
    @PatchMapping("/{locationId}")
    public ApiResponse<LocationResponse> updateLocation(
            @PathVariable Long locationId,
            @RequestBody LocationUpdateRequest request
            ) {
        return ApiResponse.ok(locationService.updateLocation(locationId, request));
    }

    /**
     * 위치 삭제 API
     * @param locationId 위치 ID (PathVariable)
     * @return 204 NO_CONTENT HTTP 상태 코드
     */
    @PatchMapping("/{locationId}/delete")
    public ApiResponse<LocationResponse> deleteLocation(
            @PathVariable Long locationId
    ){
        return ApiResponse.of(HttpStatus.NO_CONTENT, locationService.deleteLocation(locationId));
    }
}

