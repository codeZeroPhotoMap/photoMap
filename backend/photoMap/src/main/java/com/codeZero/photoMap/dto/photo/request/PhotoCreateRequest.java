package com.codeZero.photoMap.dto.photo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PhotoCreateRequest {

    @NotBlank(message = "파일명은 필수입니다.")
    private String fileName;

    @NotBlank(message = "파일 확장자명은 필수입니다.")
    private String fileExtension;

    public PhotoServiceRequest toServiceRequest() {
        return PhotoServiceRequest.builder()
                .fileName(fileName)
                .fileExtension(fileExtension)
                .build();
    }
}
