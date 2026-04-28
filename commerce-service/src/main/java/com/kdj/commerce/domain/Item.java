package com.kdj.commerce.domain;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Item {
    private Long id;
    private String name;
    private Integer price;
    private Integer Quantity;
    private String description;

    public Item() {
    }
}
