package com.kdj.commerce.service;

import com.kdj.commerce.domain.walk.*;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WalkCourseHitServiceTest {
    private final WalkCourseRepository courses = mock(WalkCourseRepository.class);
    private final WalkCourseService service = new WalkCourseService(
            courses, mock(KakaoRouteService.class));

    @Test
    @DisplayName("인기 코스는 상위 30개 안에서만 페이지를 나눈다")
    void paginatesOnlyTopThirtyAndRejectsAccessBeyondRanking() {
        var ranking = IntStream.range(0, 30).mapToObj(i -> mock(WalkCourse.class)).toList();
        when(courses.findTop30ByOrderByAverageRatingDescRatingCountDescCreatedAtDescIdDesc()).thenReturn(ranking);

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
    @DisplayName("인기 코스가 적거나 없어도 페이지를 정상 조회한다")
    void handlesShortAndEmptyRankings() {
        var ranking = IntStream.range(0, 5).mapToObj(i -> mock(WalkCourse.class)).toList();
        when(courses.findTop30ByOrderByAverageRatingDescRatingCountDescCreatedAtDescIdDesc()).thenReturn(ranking);
        var last = service.findBest(PageRequest.of(1, 3));
        assertThat(last.getContent()).containsExactlyElementsOf(ranking.subList(3, 5));
        assertThat(last.getTotalElements()).isEqualTo(5);
        assertThat(last.getTotalPages()).isEqualTo(2);

        when(courses.findTop30ByOrderByAverageRatingDescRatingCountDescCreatedAtDescIdDesc()).thenReturn(List.of());
        assertThat(service.findBest(PageRequest.of(0, 3)).getContent()).isEmpty();
    }
}
