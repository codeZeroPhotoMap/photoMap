package com.codeZero.photoMap.dto.member.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoUserInfoResponse {

    private String email;
    private String nickname;

}
