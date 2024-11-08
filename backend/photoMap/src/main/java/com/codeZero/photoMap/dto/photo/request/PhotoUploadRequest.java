package com.codeZero.photoMap.dto.photo.request;


import jakarta.validation.constraints.NotBlank;

public class PhotoUploadRequest {

    @NotBlank(message = "업로드 여부는 필수입니다.")
    private boolean uploadStatus;

    public boolean isUploadStatus() {
        return this.uploadStatus;
    }
}
