package com.kdj.commerce.domain;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Item {
    private Long id;
    private String name;
    private Integer price;
    private Integer quantity;
    private String description;

    private boolean open;
    private String delivery;

    public Item() {
    }
}
