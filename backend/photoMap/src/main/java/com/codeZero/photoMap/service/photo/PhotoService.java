package com.codeZero.photoMap.service.photo;

import com.codeZero.photoMap.common.exception.IllegalArgumentException;
import com.codeZero.photoMap.common.exception.NotFoundException;
import com.codeZero.photoMap.domain.location.Location;
import com.codeZero.photoMap.domain.location.LocationRepository;
import com.codeZero.photoMap.domain.member.Member;
import com.codeZero.photoMap.domain.member.MemberRepository;
import com.codeZero.photoMap.domain.photo.Photo;
import com.codeZero.photoMap.domain.photo.PhotoRepository;
import com.codeZero.photoMap.dto.photo.request.PhotoServiceRequest;
import com.codeZero.photoMap.dto.photo.request.PhotoUpdateRequest;
import com.codeZero.photoMap.dto.photo.request.PhotoUploadRequest;
import com.codeZero.photoMap.dto.photo.response.PhotoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final S3PreSignedUrlService s3PreSignedUrlService;
    private final MemberRepository memberRepository;
    private final LocationRepository locationRepository;

    /**
     * 사진 생성
     * @param memberId 멤버 ID
     * @param locationId 위치 ID
     * @param request 사진 생성 요청 DTO (파일명, 파일 확장자명)
     * @return PhotoResponse 생성된 사진 응답 DTO (URL - PUT 메서드용 Pre-signed URL)
     */
    public PhotoResponse createPhoto(Long memberId, Long locationId, PhotoServiceRequest request) {

        // 동일한 파일명 업로드 시도 시 덮어쓰기를 방지하기 위해 UUID를 활용하여 fileKey 생성
        String uuid = UUID.randomUUID().toString();
        String dirName = "photos"; // S3 내 폴더 명 (photos)
        String fileKey = dirName + "/" + uuid + "_" + request.getFileName(); // fileKey: "photos/uuid_파일명.파일확장자명"

        // PUT 메서드용 S3 Pre-signed URL 발급
        String preSignedUrl = s3PreSignedUrlService.getPreSignedUploadUrl(fileKey, request.getFileExtension());

        // Member ID로 Member 객체 조회
        Member member = getMember(memberId);

        // Location ID로 Location 객체 조회
        Location location = getLocation(locationId);

        // Photo 객체 생성 및 저장 (아직 업로드 되기 전이므로 uploadStatus 값은 default 값인 false로 설정)
        Photo photo = request.toEntity(member, location, fileKey);
        Photo savedPhoto = photoRepository.save(photo);

        // 생성 및 저장된 Photo 객체와 PUT 메서드용 Pre-signed URL을 포함한 PhotoResponse 반환
        return PhotoResponse.of(savedPhoto, preSignedUrl);

        /*
        try {
            // S3에 파일 업로드
            String uploadImageUrl = s3Uploader.uploadFile(multipartFile, dirName, uuid);
            log.info("S3 업로드 성공: {}", uploadImageUrl);

            // 업로드된 URL을 바탕으로 Photo 객체 생성
            Photo photo = Photo.builder()
                    .memberId(memberId)
                    .locationId(locationId)
                    .fileName(fileName)
                    .fileExtension(fileExtension)
                    .url(uploadImageUrl)
                    .build();

            Photo savedPhoto = photoRepository.save(photo);
            log.info("Photo 객체 저장 성공: ID = {}", savedPhoto.getId());

            // 생성된 사진 응답 DTO 반환
            return PhotoResponse.of(savedPhoto);

        } catch (IOException e) {
            // 업로드 실패 시 예외 처리
            log.error("S3 업로드 중 IOException 발생: {}", e.getMessage(), e);
            throw new ServerErrorException("파일 업로드에 실패했습니다.");
        }
        */
    }

    /**
     * 사진 업로드 후 클라이언트에게서 upload 여부를 받아 Photo 객체 업데이트
     * @param photoId 사진 ID
     * @param request 사진 업로드 요청 DTO (업로드 여부 - boolean)
     * @return PhotoResponse 사진 응답 DTO (URL - GET 메서드용 Pre-signed URL)
     */
    public PhotoResponse uploadPhoto(Long photoId, PhotoUploadRequest request) {

        // PhotoUploadRequest 내 uploadStatus 값이 false인 경우, IllegalArgumentException 발생
        if (!request.isUploadStatus()) {
            throw new IllegalArgumentException("파일을 업로드해주세요!");
        }

        // photoId로 Photo 객체 조회
        Photo photo = getPhoto(photoId);

        photo.uploaded(); // 조회된 Photo 객체의 uploadStatus 값을 true로 설정
        Photo savedPhoto = photoRepository.save(photo); // 업로드 완료된 Photo 객체 저장

        // 업로드 완료된 Photo 객체와 GET 메서드용 Pre-signed URL을 포함한 PhotoResponse 반환
        return PhotoResponse.of(savedPhoto, s3PreSignedUrlService.getPreSignedGetUrl(photo.getFileKey(), photo.getFileExtension()));
    }

    /**
     * 사진 ID로 사진 조회 URL 발급
     * @param photoId 사진 ID
     * @return PhotoResponse 사진 응답 DTO
     */
    public PhotoResponse getPhotoById(Long photoId) {

        // Photo ID로 업로드가 완료된 Photo 객체 조회 (uploadStatus = true)
        Photo photo = getUploadedPhoto(photoId);

        // 조회된 Photo 객체와 GET 메서드용 Pre-signed URL을 포함한 PhotoResponse 반환
        return PhotoResponse.of(photo, s3PreSignedUrlService.getPreSignedGetUrl(photo.getFileKey(), photo.getFileExtension()));
    }

    /**
     * 멤버 ID로 사진 조회 URL 리스트 발급
     * @param memberId 멤버 ID
     * @return List<PhotoResponse> 사진 응답 DTO 리스트
     */
    public List<PhotoResponse> getPhotosByMemberId(Long memberId) {

        // memberId로 사진 객체 리스트 조회
        List<Photo> photos = photoRepository.findByMemberIdAndIsDeletedFalseAndUploadStatusTrue(memberId);

        // 조회된 Photo 객체들과 이에 대한 GET 메서드용 Pre-signed URL을 포함한 PhotoResponse 리스트 반환
        return photos.stream()
                .map(p -> PhotoResponse.of(p, s3PreSignedUrlService.getPreSignedGetUrl(p.getFileKey(), p.getFileExtension())))
                .collect(Collectors.toList());
    }

    /**
     * 위치 ID로 사진 조회 URL 리스트 발급
     * @param locationId 위치 ID
     * @return List<PhotoResponse> 사진 응답 DTO 리스트
     */
    public List<PhotoResponse> getPhotosByLocationId(Long locationId) {

        // locationId로 사진 객체 리스트 조회
        List<Photo> photos = photoRepository.findByLocationIdAndIsDeletedFalseAndUploadStatusTrue(locationId);

        // 조회된 Photo 객체들과 이에 대한 Get 메서드용 Pre-signed URL을 포함한 PhotoResponse 리스트 반환
        return photos.stream()
                .map(p -> PhotoResponse.of(p, s3PreSignedUrlService.getPreSignedGetUrl(p.getFileKey(), p.getFileExtension())))
                .collect(Collectors.toList());
    }

    /**
     * Photo 위치 ID 수정
     * @param photoId 사진 ID
     * @param request 사진 수정 요청 DTO (위치 Id 값)
     * @return PhotoResponse 사진 응답 DTO
     */
    public PhotoResponse updatePhoto(Long photoId, PhotoUpdateRequest request) {

        // Photo ID로 업로드가 완료된 Photo 객체 조회 (uploadStatus = true)
        Photo photo = getUploadedPhoto(photoId);

        // Location ID로 Location 객체 조회
        Location location = getLocation(request.getLocationId());

        photo.updatePhoto(location); // locationId 업데이트
        Photo updatedPhoto = photoRepository.save(photo); // 업데이트된 Photo 객체 저장

        // 업데이트된 Photo 객체와 Get 메서드용 Pre-signed URL을 포함한 PhotoResponse 반환
        return PhotoResponse.of(updatedPhoto, s3PreSignedUrlService.getPreSignedGetUrl(updatedPhoto.getFileKey(), updatedPhoto.getFileExtension()));
    }

    /**
     * 사진 삭제 (S3 - Hard Delete 물리 삭제, DB - Soft Delete 논리 삭제)
     * @param photoId 사진 ID
     * @return PhotoResponse 사진 응답 DTO (URL - 빈 문자열)
     */
    public PhotoResponse deletePhoto(Long photoId) {

        // Photo ID로 업로드가 완료된 Photo 객체 조회 (uploadStatus = true)
        Photo photo = getUploadedPhoto(photoId);

        // S3에서 해당 사진 제거 (Hard Delete, 물리 삭제)
        s3PreSignedUrlService.deleteFile(photo.getFileKey());

        // 조회된 Photo 객체의 isDeleted 값을 True로 설정 (Soft Delete, 논리 삭제)
        photo.delete();

        // 삭제된 Photo 객체 저장
        Photo deletedPhoto = photoRepository.save(photo);

        // 삭제된 Photo 객체와 빈 문자열의 URL을 포함한 PhotoResponse 반환
        return PhotoResponse.of(deletedPhoto, "");
    }
    
    /**
     * Member ID로 Member 객체 조회
     * @param memberId Member ID
     * @return Member 객체
     */
    private Member getMember(Long memberId) {
        return memberRepository.findByIdAndIsDeletedFalse(memberId)
                .orElseThrow(() -> new NotFoundException("해당 멤버를 찾을 수 없습니다."));
    }

    /**
     * Location ID로 Location 객체 조회
     * @param locationId Location ID
     * @return Location 객체
     */
    private Location getLocation(Long locationId) {
        return locationRepository.findByIdAndIsDeletedFalse(locationId)
                .orElseThrow(() -> new NotFoundException("해당 Location을 찾을 수 없습니다."));
    }
    
    /**
     * Photo ID로 Photo 객체 조회
     * @param photoId Photo ID
     * @return Photo 객체
     */
    private Photo getPhoto(Long photoId) {
        return photoRepository.findByIdAndIsDeletedFalse(photoId)
                .orElseThrow(() -> new NotFoundException("해당 사진 파일을 찾을 수 없습니다."));
    }

    /**
     * Photo ID로 업로드가 완료된 Photo 객체 조회 (uploadStatus = true)
     * @param photoId Photo ID
     * @return 업로드가 완료된 Photo 객체
     */
    private Photo getUploadedPhoto(Long photoId) {
        return photoRepository.findByIdAndIsDeletedFalseAndUploadStatusTrue(photoId)
                .orElseThrow(() -> new NotFoundException("해당 사진 파일을 찾을 수 없습니다."));
    }

}
