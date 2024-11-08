package com.codeZero.photoMap.controller.photo;

import com.codeZero.photoMap.common.ApiResponse;
import com.codeZero.photoMap.dto.photo.request.PhotoCreateRequest;
import com.codeZero.photoMap.dto.photo.request.PhotoUpdateRequest;
import com.codeZero.photoMap.dto.photo.request.PhotoUploadRequest;
import com.codeZero.photoMap.dto.photo.response.PhotoResponse;
import com.codeZero.photoMap.security.CustomUserDetails;
import com.codeZero.photoMap.service.photo.PhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping("/locations/{locationId}")
    public ApiResponse<PhotoResponse> createPhoto(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long locationId,
            @RequestBody PhotoCreateRequest request
    ) {
        PhotoResponse photo = photoService.createPhoto(userDetails.getId(), locationId, request.toServiceRequest());

        return ApiResponse.ok(photo);
    }

    @PostMapping("/{photoId}")
    public ApiResponse<PhotoResponse> uploadPhoto(
            @PathVariable Long photoId,
            @RequestBody PhotoUploadRequest request
    ) {
        return ApiResponse.ok(photoService.uploadPhoto(photoId, request));
    }

    @GetMapping
    public ApiResponse<List<PhotoResponse>> getPhotosByMemberId(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.ok(photoService.getPhotosByMemberId(userDetails.getId()));
    }

    @GetMapping("/{photoId}")
    public ApiResponse<PhotoResponse> getPhotoById(
            @PathVariable Long photoId
    ) {
        return ApiResponse.ok(photoService.getPhotoById(photoId));
    }

    @GetMapping("/locations/{locationId}")
    public ApiResponse<List<PhotoResponse>> getPhotosByLocationId(
            @PathVariable Long locationId
    ) {
        return ApiResponse.ok(photoService.getPhotosByLocationId(locationId));
    }

    @PatchMapping("/{photoId}")
    public ApiResponse<PhotoResponse> updatePhoto(
            @PathVariable Long photoId,
            @RequestBody PhotoUpdateRequest request
    ) {
        return ApiResponse.ok(photoService.updatePhoto(photoId, request));
    }

    @PatchMapping("/{photoId}/delete")
    public ApiResponse<PhotoResponse> deletePhoto(
            @PathVariable Long photoId
    ) {
        return ApiResponse.of(HttpStatus.NO_CONTENT, photoService.deletePhoto(photoId));
    }
}
