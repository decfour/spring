package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.domain.review.WalkCourseReview;
import com.kdj.commerce.domain.walk.WalkCourse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.stereotype.Controller;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("코스·리뷰 상세 화면")
class WalkTemplateTest {
    private final org.springframework.test.web.servlet.MockMvc mvc = createMvc();

    private org.springframework.test.web.servlet.MockMvc createMvc() {
        var templates = new org.thymeleaf.templateresolver.ClassLoaderTemplateResolver();
        templates.setPrefix("templates/");
        templates.setSuffix(".html");
        templates.setCharacterEncoding("UTF-8");
        var engine = new org.thymeleaf.spring6.SpringTemplateEngine();
        engine.setTemplateResolver(templates);
        var views = new org.thymeleaf.spring6.view.ThymeleafViewResolver();
        views.setTemplateEngine(engine);
        views.setCharacterEncoding("UTF-8");
        return org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(new PreviewController())
                .setViewResolvers(views).build();
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"guest", "member", "owner"})
    @DisplayName("비회원·일반 회원·작성자의 상세 화면과 평가 권한을 표시한다")
    void courseDetail(String viewer) throws Exception {
        String html = mvc.perform(get("/walk-preview/detail").param("viewer", viewer))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("id=\"ratingBtn\"", "평가 12명");
        if (viewer.equals("guest")) {
            assertThat(html).containsPattern("(?s)<button id=\"ratingBtn\"[^>]*disabled=\"disabled\"");
            assertThat(html).doesNotContain("id=\"ratingModal\"");
        } else if (viewer.equals("owner")) {
            assertThat(html).contains("본인 코스는 평가 할 수 없습니다.").doesNotContain("data-score=");
        } else {
            assertThat(html).contains("data-score=\"1\"", "data-score=\"5\"", "별점을 선택해주세요.");
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 4})
    @DisplayName("리뷰 작성자의 별점이 있을 때만 상세 상단에 표시한다")
    void reviewDetail(int score) throws Exception {
        String html = mvc.perform(get("/walk-preview/review").param("score", String.valueOf(score)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        if (score == 0) assertThat(html).doesNotContain("★★★★☆");
        else assertThat(html).contains("★★★★☆");
    }

    @Controller
    static class PreviewController {
        @GetMapping("/walk-preview/{view}")
        ModelAndView preview(@PathVariable String view, @RequestParam(defaultValue = "guest") String viewer,
                             @RequestParam(defaultValue = "0") int score) {
            Member author = member(1L);
            var course = WalkCourse.create(author, "숲길 산책", "코스 소개", 37.5, 127.0, 37.5, 127.01, "[]", 1200, 900);
            ReflectionTestUtils.setField(course, "id", 10L);
            ReflectionTestUtils.setField(course, "ratingCount", 12L);
            ReflectionTestUtils.setField(course, "averageRating", 4.5);
            var review = WalkCourseReview.create("산책 후기", "걷기 좋았어요.", course, author);
            ReflectionTestUtils.setField(review, "id", 20L);
            return new ModelAndView(view.equals("detail") ? "walk/detail" : "walk/review/detail")
                    .addObject("course", course).addObject("courseId", 10L).addObject("tags", List.of())
                    .addObject("loginMember", viewer.equals("guest") ? null : viewer.equals("owner") ? author : member(2L))
                    .addObject("myRating", null).addObject("review", review).addObject("member", null)
                    .addObject("authorRating", score == 0 ? null : score);
        }

        private Member member(Long id) {
            var member = Member.create("산책자", "walker@example.com", "walker", "password", MemberType.USER);
            ReflectionTestUtils.setField(member, "id", id);
            return member;
        }
    }
}
