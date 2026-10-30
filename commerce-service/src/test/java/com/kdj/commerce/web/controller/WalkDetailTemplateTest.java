package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.walk.WalkCourse;
import com.kdj.commerce.domain.walk.WalkCourseTag;
import com.kdj.commerce.domain.walk.WalkTag;
import com.kdj.commerce.service.WalkCourseService;
import com.kdj.commerce.service.WalkCourseTagService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WalkDetailTemplateTest {
    @Test
    void rendersPinControlsOnCourseDetail() throws Exception {
        var templates = new ClassLoaderTemplateResolver();
        templates.setPrefix("templates/");
        templates.setSuffix(".html");
        templates.setCharacterEncoding("UTF-8");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templates);
        var views = new ThymeleafViewResolver();
        views.setTemplateEngine(engine);
        views.setCharacterEncoding("UTF-8");
        var courses = mock(WalkCourseService.class);
        var tags = mock(WalkCourseTagService.class);
        var course = WalkCourse.create(null, "함께 걷는 숲길", "산책 코스 소개", 37.5, 127.0, 37.5, 127.01,
                "[[127.0,37.5],[127.01,37.5]]", 1000, 900);
        org.springframework.test.util.ReflectionTestUtils.setField(course, "id", 10L);
        when(courses.findById(10L)).thenReturn(course);
        when(tags.findByCourseId(10L)).thenReturn(List.of(WalkCourseTag.create(course, WalkTag.NATURE)));
        var mvc = MockMvcBuilders.standaloneSetup(new WalkController(courses, tags)).setViewResolvers(views).build();
        String html = mvc.perform(get("/walk/course/10")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("id=\"addPinBtn\"", "/js/walk-pins.js", "/css/pin.css", "id: 10");
        assertThat(html).containsPattern("<span[^>]*class=\"tag-chip\"[^>]*>\\s*자연\\s*</span>");
        Files.createDirectories(Path.of("build", "ui-pins"));
        Files.writeString(Path.of("build", "ui-pins", "walk-detail.html"), html);
    }
}
