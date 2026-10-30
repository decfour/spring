package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.walk.WalkCourse;
import com.kdj.commerce.domain.review.WalkCourseReview;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.service.WalkCourseReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WalkCourseReviewControllerTest {
    @Test
    void invalidEditKeepsEditModeAndPathReviewId() throws Exception {
        reviewCreatedBy(member(1L, MemberType.USER));
        var form = new com.kdj.commerce.web.dto.review.WalkCourseReviewForm();
        form.setId(999L);
        var errors = new org.springframework.validation.BeanPropertyBindingResult(form, "reviewForm");
        errors.rejectValue("title", "required");
        var model = new org.springframework.ui.ExtendedModelMap();
        assertThat(controller.edit(member(1L, MemberType.USER), 10L, 20L, form, errors, model))
                .isEqualTo("walk/review/form");
        assertThat(model.get("isEdit")).isEqualTo(true);
        assertThat(form.getId()).isEqualTo(20L);
        verify(service, never()).update(anyLong(), anyLong(), any(), any());
    }

    @Test
    void anotherMemberCannotEdit() throws Exception {
        reviewCreatedBy(member(1L, MemberType.USER));
        var form = new com.kdj.commerce.web.dto.review.WalkCourseReviewForm();
        assertThat(controller.edit(member(2L, MemberType.USER), 10L, 20L, form,
                new org.springframework.validation.BeanPropertyBindingResult(form, "reviewForm"),
                new org.springframework.ui.ExtendedModelMap()))
                .isEqualTo("redirect:/walk/course/10/review/20");
        verify(service, never()).update(anyLong(), anyLong(), any(), any());
    }

    private final WalkCourseReviewService service = mock(WalkCourseReviewService.class);
    private final WalkCourseReviewController controller = new WalkCourseReviewController(service);

    private Member member(long id, MemberType type) {
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(id);
        when(member.getMemberType()).thenReturn(type);
        return member;
    }

    private void reviewCreatedBy(Member creator) {
        WalkCourse course = mock(WalkCourse.class);
        
        WalkCourseReview review = WalkCourseReview.create("title", "content", course, creator);
        when(service.findById(10L, 20L)).thenReturn(review);
    }

    @Test
    @DisplayName("회원 객체가 달라도 회원 번호가 같으면 작성자가 상품 리뷰를 삭제할 수 있다")
    void creatorCanDeleteUsingAnotherMemberInstanceWithSameId() {
        reviewCreatedBy(member(1L, MemberType.USER));
        assertThat(controller.delete(member(1L, MemberType.USER), 10L, 20L))
                .isEqualTo("redirect:/walk/course/10/review");
        verify(service).delete(10L, 20L);
    }

    @Test
    @DisplayName("작성자가 아닌 일반 회원은 상품 리뷰를 삭제할 수 없다")
    void otherMemberCannotDelete() {
        reviewCreatedBy(member(1L, MemberType.USER));
        assertThat(controller.delete(member(2L, MemberType.USER), 10L, 20L))
                .isEqualTo("redirect:/walk/course/10/review/20");
        verify(service, never()).delete(anyLong(), anyLong());
    }

    @Test
    @DisplayName("관리자는 다른 회원이 작성한 상품 리뷰를 삭제할 수 있다")
    void administratorCanDelete() {
        reviewCreatedBy(member(1L, MemberType.USER));
        assertThat(controller.delete(member(2L, MemberType.ADMIN), 10L, 20L))
                .isEqualTo("redirect:/walk/course/10/review");
        verify(service).delete(10L, 20L);
    }
}

