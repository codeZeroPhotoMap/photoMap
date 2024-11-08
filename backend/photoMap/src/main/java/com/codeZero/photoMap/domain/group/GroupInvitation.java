package com.codeZero.photoMap.domain.group;

import com.codeZero.photoMap.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class GroupInvitation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;   //고유 초대 토큰
    @Column(nullable = false)
    private String email;   //초대받는 이메일
    @Column(nullable = false)
    private Long groupId;
    @Column(nullable = false)
    private LocalDateTime expiryDate;   //초대 만료 시간
    @Column(nullable = false)
    private boolean isUsed = false;   //토큰 사용 여부

    @Builder
    private GroupInvitation(Long id, String token, String email, Long groupId, LocalDateTime expiryDate, boolean isUsed) {
        this.id = id;
        this.token = token;
        this.email = email;
        this.groupId = groupId;
        this.expiryDate = expiryDate;
        this.isUsed = isUsed;
    }

    //만료 여부 확인 메서드(만료 ture, 아직유효하면 false)
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }

    //초대 토큰 사용 상태 업데이트 메서드
    public void use() {
        this.isUsed = true;
    }

}
