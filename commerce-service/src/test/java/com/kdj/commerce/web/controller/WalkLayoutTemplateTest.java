package com.kdj.commerce.web.controller;

import com.kdj.commerce.web.dto.review.WalkCourseReviewForm;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WalkLayoutTemplateTest {
    @ParameterizedTest
    @ValueSource(strings = {"main", "hit", "form", "detail", "review-list", "review-form", "review-detail", "chat-home", "chat-room"})
    void rendersSharedThemeAndRetainsCourseControls(String view) throws Exception {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        var views = new ThymeleafViewResolver();
        views.setTemplateEngine(engine);
        views.setCharacterEncoding("UTF-8");
        var mvc = MockMvcBuilders.standaloneSetup(new PreviewController()).setViewResolvers(views).build();
        String html = mvc.perform(get("/walk-layout/" + view)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("walk-page", "/css/walk.css");
        if (List.of("main", "hit", "form", "detail").contains(view)) {
            assertThat(html.split("id=\"map\"", -1)).hasSize(2);
            assertThat(html).contains("class=\"map-frame\"", "aria-label=\"산책 코스 지도\"");
        }
        if (view.equals("form")) {
            assertThat(html).contains("id=\"locationBtn\"", "id=\"submitBtn\"", "name=\"routeData\"");
        }
        if (view.equals("detail")) {
            assertThat(html.indexOf("class=\"map-frame\"")).isLessThan(html.indexOf("class=\"pin-toolbar\""));
            assertThat(html).contains("id=\"distance\"", "id=\"duration\"", "id=\"addPinBtn\"");
        }
        Files.createDirectories(Path.of("build/ui-walk"));
        Files.writeString(Path.of("build/ui-walk", view + ".html"), html);
    }

    @Controller
    static class PreviewController {
        @GetMapping("/walk-layout/{view}")
        ModelAndView preview(@PathVariable String view) {
            var creator = Map.of("id", 1L, "name", "산책자");
            var course = Map.ofEntries(Map.entry("id", 10L), Map.entry("title", "숲과 강을 따라 천천히 걷는 산책 코스"),
                    Map.entry("content", "나무 그늘을 따라 걷다가 강가에서 잠시 쉬어가세요.\n천천히 둘러보기 좋은 길입니다."),
                    Map.entry("creator", creator), Map.entry("likeCount", 12), Map.entry("distance", 1200), Map.entry("duration", 900),
                    Map.entry("startLat", 37.5), Map.entry("startLng", 127.0), Map.entry("endLat", 37.5), Map.entry("endLng", 127.01),
                    Map.entry("routeData", "[[127.0,37.5],[127.01,37.5]]"));
            var review = Map.of("id", 1L, "title", "걷기 좋은 오후의 산책", "content", "그늘이 많아서 편하게 걸었어요.",
                    "creator", creator, "createdAt", LocalDateTime.of(2026, 10, 30, 12, 0));
            var room = Map.of("id", 1L, "title", "함께 천천히 걸어요", "hostId", 1L, "hostName", "산책자", "count", 2, "joined", true, "open", true);
            var openRoom = Map.of("id", 2L, "title", "강가 한 바퀴", "hostId", 2L, "hostName", "여행자", "count", 1, "joined", false, "open", true);
            String template = view.startsWith("chat-") ? view.replace('-', '/') : "walk/" + view.replace('-', '/');
            var form = new WalkCourseReviewForm();
            form.setId(1L);
            return new ModelAndView(template).addObject("course", course).addObject("courseId", 10L)
                    .addObject("courses", new PageImpl<>(List.of(course, course, course), PageRequest.of(0, 3), 12))
                    .addObject("review", review).addObject("reviews", new PageImpl<>(List.of(review)))
                    .addObject("reviewForm", form).addObject("isEdit", false)
                    .addObject("tags", List.of(Map.of("code", "NATURE", "description", "자연"), Map.of("code", "QUIET", "description", "조용한")))
                    .addObject("mine", false).addObject("room", room).addObject("rooms", new PageImpl<>(List.of(room, openRoom)))
                    .addObject("memberId", 1L).addObject("participants", List.of(creator));
        }
    }
}
