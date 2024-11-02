package com.codeZero.photoMap.dto.member.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordUpdateRequest {

    @NotBlank(message = "Password은 필수입니다.")
    private String password;

}
