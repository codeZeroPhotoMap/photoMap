package com.codeZero.photoMap.service.photo;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Uploader {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * MultipartFile을 전달받아 File로 전환한 후 S3에 업로드
     * @param multipartFile 이미지 파일
     * @param dirName 버킷 내 저장될 디렉토리명
     * @param uuid 파일명에 추가할 uuid 값
     * @return 업로드된 파일의 S3 URL 주소
     * @throws IOException 파일 변환 중 I/O 오류가 발생하거나, S3 업로드 중 네트워크 문제가 발생할 경우
     */
    public String uploadFile(MultipartFile multipartFile, String dirName, String uuid) throws IOException { // dirName의 디렉토리가 S3 Bucket 내부에 생성됨

        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환 실패"));
        return upload(uploadFile, dirName, uuid);
    }

    /**
     * S3에 저장된 파일 삭제
     * @param fileName S3 버킷 내 파일 경로 및 이름
     * @throws RuntimeException 삭제에 실패하거나 버킷 접근에 문제가 있을 경우
     */
    public void deleteFile(String fileName) {
        try {
            amazonS3Client.deleteObject(bucket, fileName);
            log.info("S3에서 파일이 삭제되었습니다: {}", fileName);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", fileName, e);
            throw new RuntimeException("S3 파일 삭제에 실패했습니다: " + fileName, e);
        }
    }

    /**
     * MultipartFile 객체를 File 객체로 변환
     * @param file 변환할 MultipartFile 객체
     * @return 변환된 File 객체를 Optional로 반환
     * @throws IOException 파일 생성 또는 쓰기 중 I/O 오류가 발생할 경우
     */
    private Optional<File> convert(MultipartFile file) throws IOException {
        File convertFile = new File(file.getOriginalFilename()); // 업로드한 파일의 이름

        if(convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }

        return Optional.empty();
    }

    /**
     * File 객체를 S3에 업로드
     * @param uploadFile 업로드할 File 객체
     * @param dirName 버킷 내 저장될 디렉토리명
     * @return 업로드된 파일의 S3 URL 주소
     */
    private String upload(File uploadFile, String dirName, String uuid) {
        String fileName = dirName + "/" + uuid + "_" + uploadFile.getName();
        String uploadImageUrl = putS3(uploadFile, fileName);

        removeNewFile(uploadFile);  // convert()함수로 인해서 로컬에 생성된 File 삭제 (MultipartFile -> File 전환 하며 로컬에 파일 생성됨)

        return uploadImageUrl;      // 업로드된 파일의 S3 URL 주소 반환
    }

    /**
     * S3 버킷에 파일 업로드
     * @param uploadFile 업로드할 File 객체
     * @param fileName S3에 저장될 파일의 이름과 경로
     * @return 업로드된 파일의 S3 URL 주소
     */
    private String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(
                new PutObjectRequest(bucket, fileName, uploadFile)
                        .withCannedAcl(CannedAccessControlList.PublicRead)	// PublicRead 권한으로 업로드 됨
        );
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    /**
     * 로컬에 생성된 임시 파일 삭제
     * @param targetFile 삭제할 File 객체
     */
    private void removeNewFile(File targetFile) {
        if(targetFile.delete()) {
            log.info("파일이 삭제되었습니다.");
        } else {
            log.info("파일이 삭제되지 못했습니다.");
        }
    }

}
