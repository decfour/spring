package com.kdj.commerce.web.dto.purchase;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PurchaseItemForm {
    private String itemName;
    private int unitPrice;
    private int quantity;
}
