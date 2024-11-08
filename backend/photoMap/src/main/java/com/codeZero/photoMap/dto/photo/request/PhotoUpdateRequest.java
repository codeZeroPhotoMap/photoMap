package com.codeZero.photoMap.dto.photo.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PhotoUpdateRequest {

    @NotBlank(message = "위치 아이디는 필수입니다.")
    private Long locationId;
}
