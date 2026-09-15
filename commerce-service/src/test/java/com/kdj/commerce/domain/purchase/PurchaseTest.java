package com.kdj.commerce.domain.purchase;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PurchaseTest {
    @Test
    @DisplayName("주문 생성 시 상품과 금액을 설정하고 중복 취소 시 재고를 한 번만 복구한다")
    void creationLinksItemsAndCancellationRestoresStockOnlyOnce() {
        Item firstItem = mock(Item.class);
        Item secondItem = mock(Item.class);
        PurchaseItem first = PurchaseItem.create(firstItem, 1200, 2);
        PurchaseItem second = PurchaseItem.create(secondItem, 3000, 3);

        Purchase purchase = Purchase.create(mock(Member.class), "receiver", "address", first, second);

        assertThat(purchase.getPurchaseItems()).containsExactly(first, second);
        assertThat(first.getPurchase()).isSameAs(purchase);
        assertThat(second.getPurchase()).isSameAs(purchase);
        assertThat(first.getUnitPrice()).isEqualTo(1200);
        assertThat(first.getQuantity()).isEqualTo(2);
        assertThat(purchase.getTotalPrice()).isEqualTo(11400);
        assertThat(purchase.getCreatedAt()).isNotNull();
        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.ORDER);
        verify(firstItem).removeStock(2);
        verify(secondItem).removeStock(3);

        purchase.cancel();
        purchase.cancel();

        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.CANCEL);
        verify(firstItem, times(1)).addStock(2);
        verify(secondItem, times(1)).addStock(3);
    }
}
