package com.codeZero.photoMap.dto.location.response;

import com.codeZero.photoMap.domain.location.Location;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LocationResponse {

    private Long id;
    private Long groupId;
    private String name;
    private double latitude;
    private double longitude;

    public static LocationResponse of(Location location) {
        return LocationResponse.builder()
                .id(location.getId())
                .groupId(location.getGroupId())
                .name(location.getName())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .build();
    }

}
