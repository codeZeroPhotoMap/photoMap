package com.codeZero.photoMap.dto.member.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshTokenRequest {

    private String refreshToken;

}
