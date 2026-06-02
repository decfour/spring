package com.kdj.commerce.domain.item;

import lombok.Getter;

@Getter
public enum ItemType {
    BOOK("도서"), VIDEO("영상"), ETC("기타");

    private final String description;

    ItemType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
