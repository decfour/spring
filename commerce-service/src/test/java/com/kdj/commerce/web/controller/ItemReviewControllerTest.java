package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.review.ItemReview;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.service.ItemReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ItemReviewControllerTest {
    private final ItemReviewService service = mock(ItemReviewService.class);
    private final ItemReviewController controller = new ItemReviewController(service);

    private Member member(long id, MemberType type) {
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(id);
        when(member.getMemberType()).thenReturn(type);
        return member;
    }

    private void reviewCreatedBy(Member creator) {
        Item item = mock(Item.class);
        
        ItemReview review = ItemReview.create("title", "content", item, creator);
        when(service.findById(20L)).thenReturn(review);
    }

    @Test
    @DisplayName("회원 객체가 달라도 회원 번호가 같으면 작성자가 상품 리뷰를 삭제할 수 있다")
    void creatorCanDeleteUsingAnotherMemberInstanceWithSameId() {
        reviewCreatedBy(member(1L, MemberType.USER));
        assertThat(controller.delete(member(1L, MemberType.USER), 10L, 20L))
                .isEqualTo("redirect:/shop/item/10/review");
        verify(service).delete(20L);
    }

    @Test
    @DisplayName("작성자가 아닌 일반 회원은 상품 리뷰를 삭제할 수 없다")
    void otherMemberCannotDelete() {
        reviewCreatedBy(member(1L, MemberType.USER));
        assertThat(controller.delete(member(2L, MemberType.USER), 10L, 20L))
                .isEqualTo("redirect:/shop/item/10/review/20");
        verify(service, never()).delete(anyLong());
    }

    @Test
    @DisplayName("관리자는 다른 회원이 작성한 상품 리뷰를 삭제할 수 있다")
    void administratorCanDelete() {
        reviewCreatedBy(member(1L, MemberType.USER));
        assertThat(controller.delete(member(2L, MemberType.ADMIN), 10L, 20L))
                .isEqualTo("redirect:/shop/item/10/review");
        verify(service).delete(20L);
    }
}
