package com.codeZero.photoMap.dto.photo.response;

import com.codeZero.photoMap.domain.photo.Photo;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PhotoResponse {

    private Long id;
    private Long memberId;
    private Long locationId;
    private String fileName;
    private String fileExtension;
    private String fileKey;
    private String url;
    private boolean uploadStatus;

    public static PhotoResponse of(Photo photo, String url) {
        return PhotoResponse.builder()
                .id(photo.getId())
                .memberId(photo.getMember().getId())
                .locationId(photo.getLocation().getId())
                .fileName(photo.getFileName())
                .fileExtension(photo.getFileExtension())
                .fileKey(photo.getFileKey())
                .url(url)
                .uploadStatus(photo.isUploadStatus())
                .build();
    }
}
