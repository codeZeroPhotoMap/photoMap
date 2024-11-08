package com.codeZero.photoMap.dto.group.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GroupInvitationRequest {

    @NotBlank(message = "email은 필수입니다.")
    private String email;

}
