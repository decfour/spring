package com.kdj.commerce.service;

import com.kdj.commerce.domain.cart.Cart;
import com.kdj.commerce.domain.cart.CartItem;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemRepository;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import com.kdj.commerce.domain.purchase.Purchase;
import com.kdj.commerce.domain.purchase.PurchaseRepository;
import com.kdj.commerce.exception.AuthenticationRequiredException;
import com.kdj.commerce.exception.BusinessConflictException;
import com.kdj.commerce.exception.InvalidRequestException;
import com.kdj.commerce.exception.NotEnoughStockException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

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
    @DisplayName("단일 주문은 로그인 회원과 현재 가격을 사용하고 재고를 차감한다")
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
    @DisplayName("본인 장바구니를 주문하고 저장 성공 후 비운다")
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
    @DisplayName("단일·장바구니 주문 모두 로그인이 필요하다")
    void bothOrderTypesRejectMissingMemberId() {
        assertThatThrownBy(() -> service.purchase(null, 10L, 1, "name", "address"))
                .isInstanceOf(AuthenticationRequiredException.class);
        assertThatThrownBy(() -> service.purchaseCart(null, "name", "address"))
                .isInstanceOf(AuthenticationRequiredException.class);
        verifyNoInteractions(carts, purchases, items, members);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1})
    @DisplayName("주문 수량이 없거나 0 이하면 주문을 거부한다")
    void singleOrderRejectsInvalidQuantity(Integer quantity) {
        orderingMember();
        assertThatThrownBy(() -> service.purchase(1L, 10L, quantity, "name", "address"))
                .isInstanceOf(InvalidRequestException.class);
        verifyNoInteractions(items, purchases);
    }

    @Test
    @DisplayName("상품을 선택하지 않으면 주문을 거부한다")
    void singleOrderRejectsMissingItemId() {
        orderingMember();
        assertThatThrownBy(() -> service.purchase(1L, null, 1, "name", "address"))
                .isInstanceOf(InvalidRequestException.class);
        verifyNoInteractions(items, purchases);
    }

    @Test
    @DisplayName("빈 장바구니로 주문할 수 없다")
    void emptyCartCannotCreateOrder() {
        orderingMember();
        when(carts.findItem(1L)).thenReturn(List.of());
        assertThatThrownBy(() -> service.purchaseCart(1L, "name", "address"))
                .isInstanceOf(InvalidRequestException.class);
        verifyNoInteractions(purchases, items);
        verify(carts, never()).clearItem(anyLong());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("삭제되거나 판매 종료된 상품은 주문할 수 없다")
    void bothOrderTypesRejectUnavailableItem(boolean deleted) {
        orderingMember();
        Item item = sellingItem();
        item.setDeleted(deleted);
        item.setOpen(deleted);
        when(carts.findItem(1L)).thenReturn(List.of(CartItem.create(mock(Cart.class), item, 1)));

        assertThatThrownBy(() -> service.purchase(1L, 10L, 1, "name", "address"))
                .isInstanceOf(BusinessConflictException.class);
        assertThatThrownBy(() -> service.purchaseCart(1L, "name", "address"))
                .isInstanceOf(BusinessConflictException.class);
        assertThat(item.getStock()).isEqualTo(5);
        verifyNoInteractions(purchases);
        verify(carts, never()).clearItem(anyLong());
    }

    @Test
    @DisplayName("수량이 잘못된 장바구니는 주문하거나 비우지 않는다")
    void cartWithInvalidQuantityIsNotOrderedOrCleared() {
        orderingMember();
        Item item = sellingItem();
        when(carts.findItem(1L)).thenReturn(List.of(CartItem.create(mock(Cart.class), item, -1)));
        assertThatThrownBy(() -> service.purchaseCart(1L, "name", "address"))
                .isInstanceOf(InvalidRequestException.class);
        assertThat(item.getStock()).isEqualTo(5);
        verifyNoInteractions(purchases);
        verify(carts, never()).clearItem(anyLong());
    }

    @Test
    @DisplayName("재고가 부족하면 주문과 장바구니를 변경하지 않는다")
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
