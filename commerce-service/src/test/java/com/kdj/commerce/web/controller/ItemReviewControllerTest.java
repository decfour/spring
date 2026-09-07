package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.review.ItemReview;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.service.ItemReviewService;
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
    void creatorCanDeleteUsingAnotherMemberInstanceWithSameId() {
        reviewCreatedBy(member(1L, MemberType.USER));
        assertThat(controller.delete(member(1L, MemberType.USER), 10L, 20L))
                .isEqualTo("redirect:/shop/item/10/review");
        verify(service).delete(20L);
    }

    @Test
    void otherMemberCannotDelete() {
        reviewCreatedBy(member(1L, MemberType.USER));
        assertThat(controller.delete(member(2L, MemberType.USER), 10L, 20L))
                .isEqualTo("redirect:/shop/item/10/review/20");
        verify(service, never()).delete(anyLong());
    }

    @Test
    void administratorCanDelete() {
        reviewCreatedBy(member(1L, MemberType.USER));
        assertThat(controller.delete(member(2L, MemberType.ADMIN), 10L, 20L))
                .isEqualTo("redirect:/shop/item/10/review");
        verify(service).delete(20L);
    }
}
