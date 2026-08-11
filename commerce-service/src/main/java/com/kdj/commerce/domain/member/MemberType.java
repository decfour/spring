package com.kdj.commerce.domain.member;

import lombok.Getter;

@Getter
public enum MemberType {
    ADMIN("관리자"),
    SELLER("판매자"),
    USER("일반회원");

    private final String description;

    MemberType(String description) {
        this.description = description;
    }
}