package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WalkControllerTest {
    private final WalkCourseRatingService ratings = mock(WalkCourseRatingService.class);
    private final WalkController controller = new WalkController(mock(WalkCourseService.class), mock(WalkCourseTagService.class), ratings);
    private final Member member = mock(Member.class);

    @Test
    @DisplayName("별점 저장과 취소가 성공하면 204로 응답한다")
    void savesScoreAndAllowsCancellation() {
        assertThat(controller.rate(member, 10L, new WalkController.RatingRequest(4)).getStatusCode().value()).isEqualTo(204);
        verify(ratings).rate(10L, member, 4);
        assertThat(controller.cancelRating(member, 10L).getStatusCode().value()).isEqualTo(204);
        verify(ratings).cancel(10L, member);
    }

    @Test
    @DisplayName("비로그인 사용자의 별점 저장과 취소를 거부한다")
    void anonymousCannotRateOrCancel() {
        assertThat(controller.rate(null, 10L, new WalkController.RatingRequest(4)).getStatusCode().value()).isEqualTo(401);
        assertThat(controller.cancelRating(null, 10L).getStatusCode().value()).isEqualTo(401);
        verifyNoInteractions(ratings);
    }

    @Test
    @DisplayName("잘못된 별점 요청은 400으로 응답한다")
    void invalidScoreReturnsBadRequest() {
        doThrow(new IllegalArgumentException("별점은 1~5점으로 선택해주세요.")).when(ratings).rate(10L, member, 6);
        assertThat(controller.rate(member, 10L, new WalkController.RatingRequest(6)).getStatusCode().value()).isEqualTo(400);
    }
}
