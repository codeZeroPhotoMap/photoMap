package com.codeZero.photoMap.dto.location.request;

import com.codeZero.photoMap.domain.group.MemberGroup;
import com.codeZero.photoMap.domain.location.Location;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LocationServiceRequest {

    private String name;
    private double latitude;
    private double longitude;

    public Location toEntity(MemberGroup memberGroup) {
        return Location.builder()
                .memberGroup(memberGroup)
                .name(name)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
