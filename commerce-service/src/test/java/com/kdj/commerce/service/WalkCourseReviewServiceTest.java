package com.kdj.commerce.service;

import com.kdj.commerce.domain.review.WalkCourseReview;
import com.kdj.commerce.domain.review.WalkCourseReviewRepository;
import com.kdj.commerce.domain.walk.WalkCourseRepository;
import com.kdj.commerce.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalkCourseReviewServiceTest {
    private final WalkCourseReviewRepository reviews = mock(WalkCourseReviewRepository.class);
    private final WalkCourseRepository courses = mock(WalkCourseRepository.class);
    private final WalkCourseReviewService service = new WalkCourseReviewService(reviews, courses);

    @Test
    void cannotReadOrModifyReviewFromAnotherCourse() {
        when(reviews.findByIdAndCourseId(20L, 10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(10L, 20L)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.update(10L, 20L, "changed", "changed"))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.delete(10L, 20L)).isInstanceOf(ResourceNotFoundException.class);
        verify(reviews, never()).delete(any(WalkCourseReview.class));
    }

    @Test
    void updatesReviewBelongingToCourse() {
        var review = WalkCourseReview.create("title", "content", null, null);
        when(reviews.findByIdAndCourseId(20L, 10L)).thenReturn(Optional.of(review));
        service.update(10L, 20L, "new title", "new content");
        assertThat(review.getTitle()).isEqualTo("new title");
        assertThat(review.getContent()).isEqualTo("new content");
    }

    @Test
    void cannotCreateReviewForMissingCourse() {
        assertThatThrownBy(() -> service.save(10L, null, "title", "content"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(reviews, never()).save(any());
    }
}
