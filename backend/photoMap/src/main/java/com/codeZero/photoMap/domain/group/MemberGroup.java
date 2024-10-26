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

    @OneToMany(mappedBy = "memberGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberGroupMapping> members = new ArrayList<>();

    @Builder
    private MemberGroup(Long id, String groupName) {
        this.id = id;
        this.groupName = groupName;
    }

    /**
     * 그룹
     * @param groupName
     */
    public void updateGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * 멤버 추가 메서드(그룹생성시 필요)
     * @param member
     */
    public void addMember(Member member) {
        boolean alreadyExists = this.members.stream()
                .anyMatch(memberGroupMapping -> memberGroupMapping.getMember().equals(member));

        if (!alreadyExists) {
            MemberGroupMapping memberGroupMapping = MemberGroupMapping.builder()
                    .memberGroup(this)
                    .member(member)
                    .build();
            this.members.add(memberGroupMapping);
            member.getGroups().add(memberGroupMapping);
        }
    }

}

