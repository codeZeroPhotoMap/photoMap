package com.codeZero.photoMap.dto.member.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberCreateRequest {

    @NotBlank(message = "Email은 필수입니다.")
    private String email;

    @NotBlank(message = "Password은 필수입니다.")
    private String password;

    @NotBlank(message = "이름(닉네임)은 필수입니다.")
    private String name;

    @Builder
    public MemberCreateRequest(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

}

