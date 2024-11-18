package com.codeZero.photoMap.dto.group.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class GroupInvitationResponse {

    private String message;
    private String groupToken;

    @Builder
    public GroupInvitationResponse(String message, String groupToken) {
        this.message = message;
        this.groupToken = groupToken;
    }

}
