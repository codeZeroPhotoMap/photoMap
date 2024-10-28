package com.codeZero.photoMap.dto.location.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LocationReadRequest {
    @NotBlank(message = "사용자 ID는 필수입니다.")
    private Long memberId;
}
