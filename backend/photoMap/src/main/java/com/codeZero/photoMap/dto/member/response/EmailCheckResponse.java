package com.codeZero.photoMap.dto.member.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailCheckResponse {

    private boolean isDuplicate;
    private String message;

    public EmailCheckResponse(boolean isDuplicate, String message) {
        this.isDuplicate = isDuplicate;
        this.message = message;
    }

}
