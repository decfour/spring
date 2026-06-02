package com.kdj.commerce.domain.member;

import lombok.Getter;

@Getter
public enum MemberType {
    ADMIN("관리자"),       // 공지사항 작성, 시스템 통제
    SELLER("판매자"),      // 상품 등록/수정/삭제 권한
    USER("일반회원");       // 상품 조회, 주문, 장바구니 이용

    private final String description;

    MemberType(String description) {
        this.description = description;
    }
}