package com.codeZero.photoMap.dto.group.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class GroupInvitationResponse {

    private String message;
    private String token;

    @Builder
    public GroupInvitationResponse(String message, String token) {
        this.message = message;
        this.token = token;
    }

}
