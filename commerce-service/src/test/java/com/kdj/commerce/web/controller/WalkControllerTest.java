package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.WalkCourseService;
import com.kdj.commerce.service.WalkCourseTagService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WalkControllerTest {
    private final WalkCourseService service = mock(WalkCourseService.class);
    private final WalkController controller = new WalkController(service, mock(WalkCourseTagService.class));
    private final Member member = mock(Member.class);

    @Test
    @DisplayName("코스 추천에 성공하면 갱신된 추천 수를 반환한다")
    void likeReturnsUpdatedCount() {
        when(service.increaseLikeCount(10L, member)).thenReturn(3);
        var response = controller.like(member, 10L);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(3);
    }

    @Test
    @DisplayName("이미 추천한 코스는 409 상태와 중복 추천 안내를 반환한다")
    void duplicateLikeReturnsConflict() {
        when(service.increaseLikeCount(10L, member))
                .thenThrow(new IllegalStateException("이미 추천한 코스입니다"));
        var response = controller.like(member, 10L);
        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isEqualTo(Map.of("message", "이미 추천했습니다"));
    }

    @Test
    @DisplayName("비로그인 회원의 추천 요청은 401 상태를 반환하고 추천을 저장하지 않는다")
    void anonymousLikeReturnsUnauthorized() {
        var response = controller.like(null, 10L);
        assertThat(response.getStatusCode().value()).isEqualTo(401);
        verifyNoInteractions(service);
    }
}