package com.codeZero.photoMap.dto.member.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NameUpdateRequest {

    @NotBlank(message = "이름(닉네임)은 필수입니다.")
    private String name;

}
