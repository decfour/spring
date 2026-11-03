package com.kdj.commerce.service;

import com.kdj.commerce.domain.walk.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WalkCourseHitServiceTest {
    private final WalkCourseRepository courses = mock(WalkCourseRepository.class);
    private final WalkCourseService service = new WalkCourseService(
            courses, mock(KakaoRouteService.class), mock(WalkCourseLikeRepository.class));

    @Test
    void paginatesOnlyTopThirtyAndRejectsAccessBeyondRanking() {
        var ranking = IntStream.range(0, 30).mapToObj(i -> mock(WalkCourse.class)).toList();
        when(courses.findTop30ByOrderByLikeCountDescCreatedAtDescIdDesc()).thenReturn(ranking);

        var last = service.findBest(PageRequest.of(9, 3));
        assertThat(last.getContent()).containsExactlyElementsOf(ranking.subList(27, 30));
        assertThat(last.getTotalElements()).isEqualTo(30);
        assertThat(last.getTotalPages()).isEqualTo(10);
        assertThat(last.hasNext()).isFalse();
        assertThat(service.findBest(PageRequest.of(10, 3)).getContent()).isEmpty();
        assertThat(service.findBest(PageRequest.of(0, 100)).getContent()).hasSize(30);
        assertThat(service.findBest(PageRequest.of(Integer.MAX_VALUE, 100)).getContent()).isEmpty();
    }

    @Test
    void handlesShortAndEmptyRankings() {
        var ranking = IntStream.range(0, 5).mapToObj(i -> mock(WalkCourse.class)).toList();
        when(courses.findTop30ByOrderByLikeCountDescCreatedAtDescIdDesc()).thenReturn(ranking);
        var last = service.findBest(PageRequest.of(1, 3));
        assertThat(last.getContent()).containsExactlyElementsOf(ranking.subList(3, 5));
        assertThat(last.getTotalElements()).isEqualTo(5);
        assertThat(last.getTotalPages()).isEqualTo(2);

        when(courses.findTop30ByOrderByLikeCountDescCreatedAtDescIdDesc()).thenReturn(List.of());
        assertThat(service.findBest(PageRequest.of(0, 3)).getContent()).isEmpty();
    }
}
