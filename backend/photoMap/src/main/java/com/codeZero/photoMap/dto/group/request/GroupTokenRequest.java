package com.codeZero.photoMap.dto.group.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GroupTokenRequest {

    @NotBlank(message = "초대 토큰은 필수입니다.")
    private String groupToken;

}
