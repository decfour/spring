package com.kdj.commerce.domain.item;

import lombok.Getter;

@Getter
public enum DeliveryType {
    STANDARD("일반배송"),
    EXPRESS("새벽배송"),
    FREE("무료배송");

    private final String description;

    DeliveryType(String description) {
        this.description = description;
    }
}
