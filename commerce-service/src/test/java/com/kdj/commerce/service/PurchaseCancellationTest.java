package com.kdj.commerce.service;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemRepository;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import com.kdj.commerce.domain.purchase.Purchase;
import com.kdj.commerce.domain.purchase.PurchaseItem;
import com.kdj.commerce.domain.purchase.PurchaseRepository;
import com.kdj.commerce.domain.purchase.PurchaseStatus;
import com.kdj.commerce.exception.PermissionDeniedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PurchaseCancellationTest {
    private final PurchaseRepository repository = mock(PurchaseRepository.class);
    private final PurchaseService service = new PurchaseService(
            mock(CartService.class), repository,
            mock(MemberRepository.class), mock(ItemRepository.class));
    private final Item item = mock(Item.class);

    private Purchase orderOwnedBy(long memberId) {
        Member owner = mock(Member.class);
        when(owner.getId()).thenReturn(memberId);
        Purchase purchase = Purchase.create(owner, "receiver", "address",
                PurchaseItem.create(item, 1000, 2));
        when(repository.findById(10L)).thenReturn(Optional.of(purchase));
        return purchase;
    }

    @Test
    void ownerCanCancelAndRestoreStock() {
        Purchase purchase = orderOwnedBy(1L);

        service.cancel(10L, 1L);

        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.CANCEL);
        verify(item).addStock(2);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {2L})
    void unauthorizedCallerCannotChangeOrderOrStock(Long memberId) {
        Purchase purchase = orderOwnedBy(1L);

        assertThatThrownBy(() -> service.cancel(10L, memberId))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessage("본인의 주문만 취소할 수 있습니다.");

        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.ORDER);
        verify(item, never()).addStock(anyInt());
    }

    @Test
    void missingOrderIsReportedSeparatelyFromPermissionDenied() {
        assertThatThrownBy(() -> service.cancel(10L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 주문입니다.");
    }
}
