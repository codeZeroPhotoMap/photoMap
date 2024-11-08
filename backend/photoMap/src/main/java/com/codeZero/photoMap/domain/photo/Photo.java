package com.codeZero.photoMap.domain.photo;

import com.codeZero.photoMap.common.BaseEntity;
import com.codeZero.photoMap.domain.location.Location;
import com.codeZero.photoMap.domain.member.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Photo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    private String fileName;

    private String fileExtension;

    private String fileKey;

    @Builder.Default
    private boolean uploadStatus = false;

    public void updatePhoto(Location location) {
        this.location = location;
    }

    public void uploaded() {
        this.uploadStatus = true;
    }

    @Override
    public void delete() {
        super.delete();
        this.uploadStatus = false;
    }
}
