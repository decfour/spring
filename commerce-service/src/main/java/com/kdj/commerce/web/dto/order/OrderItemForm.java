package com.kdj.commerce.web.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderItemForm {
    private String itemName;
    private int orderPrice;
    private int count;
}
