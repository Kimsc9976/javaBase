package com.spring.demo.external.oauth.model;

import com.spring.demo.domain.member.constant.MemberType;
import com.spring.demo.domain.member.constant.Role;
import com.spring.demo.domain.member.entity.Member;
import groovy.transform.ToString;
import lombok.Builder;
import lombok.Getter;

@ToString
@Getter
@Builder
public class OAuthAttributes { // 회원 정보 가져올 때 통일시킴

    private String name;
    private String email;
    private String profile_path;
    private MemberType memberType;

    public Member toMemberEntity(MemberType memberType, Role role) {
        return Member.builder()
                .name(name)
                .email(email)
                .memberType(memberType)
                .profile_path(profile_path)
                .role(role)
                .build();
    }

}
