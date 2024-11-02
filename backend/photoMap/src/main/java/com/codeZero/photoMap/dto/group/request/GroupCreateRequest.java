package com.codeZero.photoMap.dto.group.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GroupCreateRequest {

    @NotBlank(message = "그룹 이름은 필수입니다.")
    private String groupName;

}
