package com.kdj.commerce.service;

import com.kdj.commerce.domain.cart.Cart;
import com.kdj.commerce.domain.cart.CartItem;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemRepository;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import com.kdj.commerce.domain.purchase.Purchase;
import com.kdj.commerce.domain.purchase.PurchaseRepository;
import com.kdj.commerce.exception.PermissionDeniedException;
import com.kdj.commerce.exception.NotEnoughStockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PurchaseCreationTest {
    private final CartService carts = mock(CartService.class);
    private final PurchaseRepository purchases = mock(PurchaseRepository.class);
    private final MemberRepository members = mock(MemberRepository.class);
    private final ItemRepository items = mock(ItemRepository.class);
    private final PurchaseService service = new PurchaseService(carts, purchases, members, items);

    private Member orderingMember() {
        Member member = mock(Member.class);
        when(members.findById(1L)).thenReturn(Optional.of(member));
        return member;
    }

    private Item sellingItem() {
        Item item = new Item();
        item.setId(10L);
        item.setPrice(1500);
        item.setStock(5);
        item.setOpen(true);
        when(items.findByIdWithLock(10L)).thenReturn(Optional.of(item));
        return item;
    }

    @Test
    void singleOrderUsesAuthenticatedMemberAndCurrentPrice() {
        Member member = orderingMember();
        Item item = sellingItem();

        service.purchase(1L, 10L, 2, "receiver", "address");

        ArgumentCaptor<Purchase> saved = ArgumentCaptor.forClass(Purchase.class);
        verify(purchases).save(saved.capture());
        assertThat(saved.getValue().getMember()).isSameAs(member);
        assertThat(saved.getValue().getTotalPrice()).isEqualTo(3000);
        assertThat(item.getStock()).isEqualTo(3);
    }

    @Test
    void cartOrderLoadsOnlyRequestingMembersCartAndClearsItAfterSaving() {
        Member member = orderingMember();
        Item currentItem = sellingItem();
        Item oldItem = new Item();
        oldItem.setId(10L);
        oldItem.setPrice(500);
        when(carts.findItem(1L)).thenReturn(List.of(CartItem.create(mock(Cart.class), oldItem, 2)));

        service.purchaseCart(1L, "receiver", "address");

        ArgumentCaptor<Purchase> saved = ArgumentCaptor.forClass(Purchase.class);
        var order = inOrder(carts, purchases);
        order.verify(carts).findItem(1L);
        order.verify(purchases).save(saved.capture());
        order.verify(carts).clearItem(1L);
        assertThat(saved.getValue().getMember()).isSameAs(member);
        assertThat(saved.getValue().getTotalPrice()).isEqualTo(3000);
        assertThat(currentItem.getStock()).isEqualTo(3);
    }

    @Test
    void bothOrderTypesRejectMissingMemberId() {
        assertThatThrownBy(() -> service.purchase(null, 10L, 1, "name", "address"))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> service.purchaseCart(null, "name", "address"))
                .isInstanceOf(PermissionDeniedException.class);
        verifyNoInteractions(carts, purchases, items, members);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1})
    void singleOrderRejectsInvalidQuantity(Integer quantity) {
        orderingMember();
        assertThatThrownBy(() -> service.purchase(1L, 10L, quantity, "name", "address"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(items, purchases);
    }

    @Test
    void singleOrderRejectsMissingItemId() {
        orderingMember();
        assertThatThrownBy(() -> service.purchase(1L, null, 1, "name", "address"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(items, purchases);
    }

    @Test
    void emptyCartCannotCreateOrder() {
        orderingMember();
        when(carts.findItem(1L)).thenReturn(List.of());
        assertThatThrownBy(() -> service.purchaseCart(1L, "name", "address"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(purchases, items);
        verify(carts, never()).clearItem(anyLong());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void bothOrderTypesRejectUnavailableItem(boolean deleted) {
        orderingMember();
        Item item = sellingItem();
        item.setDeleted(deleted);
        item.setOpen(deleted);
        when(carts.findItem(1L)).thenReturn(List.of(CartItem.create(mock(Cart.class), item, 1)));

        assertThatThrownBy(() -> service.purchase(1L, 10L, 1, "name", "address"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> service.purchaseCart(1L, "name", "address"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(item.getStock()).isEqualTo(5);
        verifyNoInteractions(purchases);
        verify(carts, never()).clearItem(anyLong());
    }

    @Test
    void cartWithInvalidQuantityIsNotOrderedOrCleared() {
        orderingMember();
        Item item = sellingItem();
        when(carts.findItem(1L)).thenReturn(List.of(CartItem.create(mock(Cart.class), item, -1)));
        assertThatThrownBy(() -> service.purchaseCart(1L, "name", "address"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(item.getStock()).isEqualTo(5);
        verifyNoInteractions(purchases);
        verify(carts, never()).clearItem(anyLong());
    }

    @Test
    void insufficientStockDoesNotSaveOrderOrClearCart() {
        orderingMember();
        Item item = sellingItem();
        when(carts.findItem(1L)).thenReturn(List.of(CartItem.create(mock(Cart.class), item, 6)));
        assertThatThrownBy(() -> service.purchaseCart(1L, "name", "address"))
                .isInstanceOf(NotEnoughStockException.class);
        assertThat(item.getStock()).isEqualTo(5);
        verifyNoInteractions(purchases);
        verify(carts, never()).clearItem(anyLong());
    }
}
