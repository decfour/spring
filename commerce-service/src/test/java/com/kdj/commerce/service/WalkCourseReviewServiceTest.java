package com.kdj.commerce.service;

import com.kdj.commerce.domain.review.WalkCourseReview;
import com.kdj.commerce.domain.review.WalkCourseReviewRepository;
import com.kdj.commerce.domain.walk.WalkCourseRepository;
import com.kdj.commerce.exception.ResourceNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalkCourseReviewServiceTest {
    private final WalkCourseReviewRepository reviews = mock(WalkCourseReviewRepository.class);
    private final WalkCourseRepository courses = mock(WalkCourseRepository.class);
    private final WalkCourseReviewService service = new WalkCourseReviewService(reviews, courses, mock(WalkCourseRatingService.class));

    @Test
    @DisplayName("다른 코스에 속한 리뷰를 조회하거나 변경할 수 없다")
    void cannotReadOrModifyReviewFromAnotherCourse() {
        when(reviews.findByIdAndCourseId(20L, 10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(10L, 20L)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.update(10L, 20L, "changed", "changed"))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.delete(10L, 20L)).isInstanceOf(ResourceNotFoundException.class);
        verify(reviews, never()).delete(any(WalkCourseReview.class));
    }

    @Test
    @DisplayName("없는 코스에는 리뷰를 작성할 수 없다")
    void cannotCreateReviewForMissingCourse() {
        assertThatThrownBy(() -> service.save(10L, null, "title", "content"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(reviews, never()).save(any());
    }
}

