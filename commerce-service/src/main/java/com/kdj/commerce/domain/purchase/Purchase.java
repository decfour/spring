package com.kdj.commerce.domain.purchase;

import com.kdj.commerce.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private PurchaseStatus status;

    private String receiverName;
    private String receiverAddress;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL)
    private List<PurchaseItem> purchaseItems = new ArrayList<>();

    public static Purchase create(Member member,
                               String receiverName,
                               String receiverAddress,
                               PurchaseItem... purchaseItems) {
        Purchase purchase = new Purchase();

        purchase.member = member;
        purchase.receiverName = receiverName;
        purchase.receiverAddress = receiverAddress;
        purchase.status = PurchaseStatus.ORDER;

        for (PurchaseItem purchaseItem : purchaseItems) {
            purchase.addPurchaseItem(purchaseItem);
        }

        return purchase;
    }

    public void addPurchaseItem(PurchaseItem purchaseItem) {
        purchaseItems.add(purchaseItem);
        purchaseItem.assignPurchase(this);
    }

    public void cancel() {
        if (status == PurchaseStatus.CANCEL) {
            return;
        }

        this.status = PurchaseStatus.CANCEL;

        for (PurchaseItem purchaseItem : purchaseItems) {
            purchaseItem.cancel();
        }
    }

    public int getTotalPrice() {
        int totalPrice = 0;

        for (PurchaseItem purchaseItem : purchaseItems) {
            totalPrice += purchaseItem.getTotalPrice();
        }

        return totalPrice;
    }

}
