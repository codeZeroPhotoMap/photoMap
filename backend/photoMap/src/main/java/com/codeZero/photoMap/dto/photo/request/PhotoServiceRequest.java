package com.codeZero.photoMap.dto.photo.request;

import com.codeZero.photoMap.domain.location.Location;
import com.codeZero.photoMap.domain.member.Member;
import com.codeZero.photoMap.domain.photo.Photo;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PhotoServiceRequest {

    private String fileName;
    private String fileExtension;

    public Photo toEntity(Member member, Location location, String fileKey) {
        return Photo.builder()
                .member(member)
                .location(location)
                .fileName(fileName)
                .fileExtension(fileExtension)
                .fileKey(fileKey)
                .build();
    }
}
