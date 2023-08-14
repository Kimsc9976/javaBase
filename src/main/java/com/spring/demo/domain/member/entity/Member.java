package com.spring.demo.domain.member.entity;

import com.spring.demo.domain.common.entity.BaseEntity;
import com.spring.demo.domain.member.constant.Role;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Member extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(unique = true, length = 50, nullable = false)
    private String email;

    @Column
    private String password;

    @Column
    private String name;

    @Column
    private String nickname;

    @Column
    private String profile_path;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    @Column
    private String refreshToken;

    private LocalDateTime tokenExpirationTime;

    @Builder
    public Member(String email, String password, String name, String nickname, String profile_path, Role role){
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.profile_path = profile_path;
        this.role = role;
    }
    public void updateMemberToken(String refreshToken){
        this.refreshToken = refreshToken;
    }
}
