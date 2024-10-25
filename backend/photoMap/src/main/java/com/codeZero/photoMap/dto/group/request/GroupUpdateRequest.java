package com.codeZero.photoMap.dto.group.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GroupUpdateRequest {

    @NotBlank(message = "그룹 이름은 필수입니다.")
    private String groupName;

    public GroupUpdateRequest(String groupName) {
        this.groupName = groupName;
    }
}