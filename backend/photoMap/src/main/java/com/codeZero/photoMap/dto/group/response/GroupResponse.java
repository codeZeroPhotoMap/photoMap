package com.codeZero.photoMap.dto.group.response;

import com.codeZero.photoMap.domain.group.MemberGroup;
import com.codeZero.photoMap.domain.member.Member;
import com.codeZero.photoMap.dto.member.response.MemberResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class GroupResponse {
    private Long id;
    private String name;
    private List<MemberResponse> members;

    @Builder
    public GroupResponse(Long id, String name, List<MemberResponse> members) {
        this.id = id;
        this.name = name;
        this.members = members;
    }

    //그룹정보에 멤버들 포함
    public static GroupResponse from(MemberGroup memberGroup, List<Member> members) {
        List<MemberResponse> memberResponses = members.stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());

        return GroupResponse.builder()
                .id(memberGroup.getId())
                .name(memberGroup.getGroupName())
                .members(memberResponses)
                .build();
    }

    //멤버리스트 없이 그룹만 처리
    public static GroupResponse of(MemberGroup memberGroup) {
        return GroupResponse.builder()
                .id(memberGroup.getId())
                .name(memberGroup.getGroupName())
                .build();
    }

}
