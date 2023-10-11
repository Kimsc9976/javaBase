package com.spring.demo.domain.member.constant;

public enum Role {

    USER, ADMIN, SUBSCRIBER;

    public static Role from(String role) {
        return Role.valueOf(role);
    }

}