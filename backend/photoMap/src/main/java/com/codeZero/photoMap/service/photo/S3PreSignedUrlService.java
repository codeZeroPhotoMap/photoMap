package com.codeZero.photoMap.service.photo;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3PreSignedUrlService {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 업로드용 S3 pre-signed URL 생성
     * @param fileKey 파일 key 값 (S3 버킷 내 파일 경로 및 이름, 폴더명/UUID_파일명.확장자)
     * @param fileExtension 파일 확장자명
     * @return PUT 메서드용 S3 pre-signed URL (유효 기간 10분)
     */
    public String getPreSignedUploadUrl(String fileKey, String fileExtension) {

        // 유효 기간 설정 (10분)
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime() + (10 * 60 * 1000);
        expiration.setTime(expTimeMillis);

        // Pre-Signed URL 생성 (PUT 요청용)
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, fileKey)
                        .withMethod(HttpMethod.PUT)
                        .withKey(fileKey)
//                        .withContentType("image/" + fileExtension)  // 이미지 파일 조회할 때 클라이언트에서 헤더에 Content-Type 지정하지 않아도 조회 가능하도록 주석 처리함.
                        .withExpiration(expiration);

/*
        // 업로드 이후 pre-signed URL에 query param을 제거하고 GET 요청을 하면 파일을 보거나 다운받을 수 있도록
        // pre-signed URL에 public read 권한 부여
        generatePresignedUrlRequest.addRequestParameter(
                Headers.S3_CANNED_ACL, CannedAccessControlList.PublicRead.toString());
*/

        return amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest).toString();
    }

    /**
     * 사진 조회용 S3 pre-signed URL 생성
     * @param fileKey 파일 key 값 (S3 버킷 내 파일 경로 및 이름, 폴더명/UUID_파일명.확장자)
     * @param fileExtension 파일 확장자명
     * @return GET 메서드용 S3 pre-signed URL (유효 기간 60분)
     */
    public String getPreSignedGetUrl(String fileKey, String fileExtension) {

        // 유효 기간 설정 (60분)
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime() + (60 * 60 * 1000);
        expiration.setTime(expTimeMillis);

        // Pre-Signed URL 생성 (GET 요청용)
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, fileKey)
                        .withMethod(HttpMethod.GET)
                        .withKey(fileKey)
//                        .withContentType("image/" + fileExtension) // 이미지 파일 조회할 때 클라이언트에서 헤더에 Content-Type 지정하지 않아도 조회 가능하도록 주석 처리함.
                        .withExpiration(expiration);

        return amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest).toString();
    }

    /**
     * S3에 저장된 파일 삭제
     * @param fileKey 파일 key 값 (S3 버킷 내 파일 경로 및 이름, 폴더명/UUID_파일명.확장자)
     * @throws RuntimeException 삭제에 실패하거나 버킷 접근에 문제가 있을 경우
     */
    public void deleteFile(String fileKey) {
        try {
            amazonS3Client.deleteObject(bucket, fileKey);
            log.info("S3에서 파일이 삭제되었습니다: {}", fileKey);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", fileKey, e);
            throw new RuntimeException("S3 파일 삭제에 실패했습니다: " + fileKey, e);
        }
    }
}
