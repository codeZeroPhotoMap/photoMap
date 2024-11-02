package com.codeZero.photoMap.domain.member;

import com.codeZero.photoMap.common.BaseEntity;
import com.codeZero.photoMap.domain.group.MemberGroupMapping;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)  // Enum 값을 문자열로 저장
    private MemberRole role;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberGroupMapping> groups = new ArrayList<>();


    @Builder
    private Member(Long id, String email, String password, String name, MemberRole role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    /**
     * 멤버 이름 업데이트
     * @param name
     */
    public void updateName(String name) {
        this.name = name;
    }

    /**
     * 멤버 비밀번호 업데이트
     * @param password
     */
    public void updatePassword(String password) {
        this.password = password;
    }
}
