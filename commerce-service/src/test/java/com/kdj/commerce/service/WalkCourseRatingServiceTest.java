package com.kdj.commerce.service;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.walk.*;
import com.kdj.commerce.exception.BusinessConflictException;
import jakarta.persistence.*;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalkCourseRatingServiceTest {
    private final WalkCourseRatingRepository ratings = mock(WalkCourseRatingRepository.class);
    private final EntityManager em = mock(EntityManager.class);
    private final WalkCourseRatingService service = new WalkCourseRatingService(ratings, em);
    private final Member member = mock(Member.class);

    private WalkCourse course(Member owner) {
        when(member.getId()).thenReturn(2L);
        var course = WalkCourse.create(owner, "코스", "소개", 37.5, 127.0, 37.5, 127.0, "[]", 1000, 600);
        when(em.find(WalkCourse.class, 10L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(course);
        return course;
    }

    @Test
    @DisplayName("리뷰 없이 별점을 남기고 점수를 바꿔도 평가 수는 늘지 않는다")
    void savesWithoutReviewAndUpdatesSameVote() {
        var course = course(null);
        service.rate(10L, member, 5);
        var saved = org.mockito.ArgumentCaptor.forClass(WalkCourseRating.class);
        verify(ratings).save(saved.capture());
        assertThat(saved.getValue().getScore()).isEqualTo(5);
        assertThat(course.getRatingCount()).isEqualTo(1);
        when(ratings.findByCourseIdAndMemberId(10L, 2L)).thenReturn(Optional.of(saved.getValue()));
        service.rate(10L, member, 2);
        assertThat(saved.getValue().getScore()).isEqualTo(2);
        assertThat(course.getRatingCount()).isEqualTo(1);
        verify(ratings, times(2)).save(saved.getValue());
    }

    @Test
    @DisplayName("별점은 1점부터 5점까지만 허용한다")
    void rejectsInvalidScoresBeforeDatabaseAccess() {
        for (Integer score : new Integer[]{null, 0, 6})
            assertThatThrownBy(() -> service.rate(10L, member, score)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(ratings, em);
    }

    @Test
    @DisplayName("본인 코스에는 별점을 남길 수 없다")
    void ownerCannotRateOwnCourse() {
        course(member);
        assertThatThrownBy(() -> service.rate(10L, member, 5)).isInstanceOf(BusinessConflictException.class);
        verifyNoInteractions(ratings);
    }

    @Test
    @DisplayName("본인 평가를 취소하면 평가 수를 줄인다")
    void cancellationRemovesOnlyMembersRating() {
        var course = course(null);
        course.addRating();
        var rating = WalkCourseRating.create(course, member, 4);
        when(ratings.findByCourseIdAndMemberId(10L, 2L)).thenReturn(Optional.of(rating));
        service.cancel(10L, member);
        verify(ratings).delete(rating);
        assertThat(course.getRatingCount()).isZero();
    }

    @Test
    @DisplayName("취소할 평가가 없으면 평가 수를 유지한다")
    void cancellingMissingRatingDoesNotChangeCount() {
        var course = course(null);
        course.addRating();
        service.cancel(10L, member);
        assertThat(course.getRatingCount()).isEqualTo(1);
        verify(ratings, never()).delete(any());
    }
}
