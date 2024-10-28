package com.codeZero.photoMap.dto.location.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocationUpdateRequest {
    @NotBlank(message = "폴더명은 필수입니다.")
    private String name;

    @NotBlank(message = "위도 값은 필수입니다.")
    private double latitude;

    @NotBlank(message = "경도 값은 필수입니다.")
    private double longitude;
}
