package com.codeZero.photoMap.domain.group;

import com.codeZero.photoMap.common.BaseEntity;
import com.codeZero.photoMap.domain.member.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class MemberGroupMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private MemberGroup memberGroup;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public MemberGroupMapping(MemberGroup memberGroup, Member member) {

        this.memberGroup = memberGroup;
        this.member = member;
    }
}
