package com.kdj.commerce.domain.purchase;

import com.kdj.commerce.domain.item.Item;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "purchase_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PurchaseItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id")
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private int unitPrice;
    private int quantity;

    private PurchaseItem(Item item, int unitPrice, int quantity) {
        this.item = item;
        this.unitPrice = unitPrice;
        this.quantity = quantity;

        item.removeStock(quantity);
    }

    public static PurchaseItem create(Item item, int unitPrice, int quantity) {
        return new PurchaseItem(item, unitPrice, quantity);
    }

    public void assignPurchase(Purchase purchase) {
        this.purchase = purchase;
    }

    public void cancel() {
        item.addStock(quantity);
    }

    public int getTotalPrice() {
        return unitPrice * quantity;
    }
}
