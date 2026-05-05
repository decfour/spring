package com.kdj.commerce.domain.item;

import lombok.Data;

@Data
public class Item {
    private Long id;
    private String name;
    private Integer price;
    private Integer quantity;
    private String description;

    private boolean open;
    private ItemType itemType;
    private String delivery;

    public Item() {
    }
}
