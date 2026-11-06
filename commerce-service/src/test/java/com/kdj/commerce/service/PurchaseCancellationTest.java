package com.kdj.commerce.service;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemRepository;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import com.kdj.commerce.domain.purchase.Purchase;
import com.kdj.commerce.domain.purchase.PurchaseItem;
import com.kdj.commerce.domain.purchase.PurchaseRepository;
import com.kdj.commerce.domain.purchase.PurchaseStatus;
import com.kdj.commerce.exception.AuthenticationRequiredException;
import com.kdj.commerce.exception.PermissionDeniedException;
import com.kdj.commerce.exception.ResourceNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
    @DisplayName("본인 주문을 취소하면 재고를 복구한다")
    void ownerCanCancelAndRestoreStock() {
        Purchase purchase = orderOwnedBy(1L);

        service.cancel(10L, 1L);

        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.CANCEL);
        verify(item).addStock(2);
    }

    @ParameterizedTest
    @ValueSource(longs = {2L})
    @DisplayName("다른 회원은 주문이나 재고를 변경할 수 없다")
    void unauthorizedCallerCannotChangeOrderOrStock(Long memberId) {
        Purchase purchase = orderOwnedBy(1L);

        assertThatThrownBy(() -> service.cancel(10L, memberId))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessage("본인의 주문만 취소할 수 있습니다.");

        assertThat(purchase.getStatus()).isEqualTo(PurchaseStatus.ORDER);
        verify(item, never()).addStock(anyInt());
    }

    @Test
    @DisplayName("비로그인 사용자는 주문을 조회하거나 취소할 수 없다")
    void anonymousCallerCannotCancelOrLoadOrder() {
        assertThatThrownBy(() -> service.cancel(10L, null))
                .isInstanceOf(AuthenticationRequiredException.class);
        verifyNoInteractions(repository, item);
    }

    @Test
    @DisplayName("없는 주문과 권한 없는 주문을 구분한다")
    void missingOrderIsReportedSeparatelyFromPermissionDenied() {
        assertThatThrownBy(() -> service.cancel(10L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("존재하지 않는 주문입니다.");
    }
}
