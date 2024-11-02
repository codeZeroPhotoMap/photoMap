package com.codeZero.photoMap.domain.group;

import com.codeZero.photoMap.common.BaseEntity;
import com.codeZero.photoMap.domain.member.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class MemberGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;

    @Column(nullable = false)
    private String groupName;

    @Column(nullable = false)
    private Long ownerId;

    //나만의 그룹인가 아닌가
    @Column(nullable = false)
    private boolean isPersonal;

    @OneToMany(mappedBy = "memberGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberGroupMapping> members = new ArrayList<>();

    @Builder
    private MemberGroup(Long id, String groupName, Long ownerId, boolean isPersonal) {
        this.id = id;
        this.groupName = groupName;
        this.ownerId = ownerId;
        this.isPersonal = isPersonal;
    }

    /**
     * 그룹 이름 업데이트
     * @param groupName
     */
    public void updateGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * 맴버 추가 메서드(그룹 생성시 필요)
     * @param member
     * @param role
     */
    public void addMemberWithRole(Member member, Role role) {
        MemberGroupMapping memberGroupMapping = MemberGroupMapping.builder()
                .memberGroup(this)
                .member(member)
                .role(role)
                .build();
        this.members.add(memberGroupMapping);
        member.getGroups().add(memberGroupMapping);
    }

}

