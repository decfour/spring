package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.ChatRoomService;
import com.kdj.commerce.web.dto.chat.*;
import com.kdj.commerce.web.argumentresolver.Login;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

class ChatRoomControllerTest {
    private final ChatRoomService service = mock(ChatRoomService.class);
    private Member loginMember;
    private MockMvc mvc;
    private final ChatCourseResponse course = new ChatCourseResponse(10L, "한강 산책", 3200, 3000);

    @BeforeEach
    void setUp() {
        loginMember = mock(Member.class);
        when(loginMember.getId()).thenReturn(1L);
        ClassLoaderTemplateResolver templates = new ClassLoaderTemplateResolver();
        templates.setPrefix("templates/");
        templates.setSuffix(".html");
        templates.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templates);
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(engine);
        resolver.setCharacterEncoding("UTF-8");
        mvc = MockMvcBuilders.standaloneSetup(new ChatRoomController(service))
                .setViewResolvers(resolver)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    public boolean supportsParameter(MethodParameter p) { return p.hasParameterAnnotation(Login.class); }
                    public Object resolveArgument(MethodParameter p, ModelAndViewContainer m,
                                                  NativeWebRequest r, WebDataBinderFactory b) { return loginMember; }
                }).build();
    }

    @Test
    void homeRendersDatabaseRoomsAndPagination() throws Exception {
        when(service.findCourse(10L)).thenReturn(course);
        when(service.findRooms(eq(10L), eq(1L), eq(false), any())).thenReturn(new PageImpl<>(List.of(
                new ChatRoomResponse(20L, "<script>제목</script>", "민서", 2L, true, 3, false),
                new ChatRoomResponse(21L, "정원이 찬 방", "민서", 2L, true, 5, false)
        ), PageRequest.of(0, 4), 12));
        mvc.perform(get("/walk/course/10/chat"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/walk/course/10/chat/20/join")))
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(containsString("정원 마감")))
                .andExpect(content().string(not(containsString("모집 중"))))
                .andExpect(content().string(containsString("page=1")));
    }

    @Test
    void emptyJoinedListRenders() throws Exception {
        when(service.findCourse(10L)).thenReturn(course);
        when(service.findRooms(eq(10L), eq(1L), eq(true), any())).thenReturn(new PageImpl<>(List.of()));
        mvc.perform(get("/walk/course/10/chat").param("mine", "true"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("참여 중인 채팅방이 없습니다")));
    }

    @Test
    void createUsesAuthenticatedMemberNotSubmittedId() throws Exception {
        when(service.save(10L, 1L, "새 모임")).thenReturn(20L);
        mvc.perform(post("/walk/course/10/chat").param("title", "새 모임").param("memberId", "99"))
                .andExpect(redirectedUrl("/walk/course/10/chat/20"));
        verify(service).save(10L, 1L, "새 모임");
    }

    @Test
    void fullRoomShowsError() throws Exception {
        doThrow(new IllegalStateException("정원이 찼습니다.")).when(service).join(10L, 20L, 1L);
        mvc.perform(post("/walk/course/10/chat/20/join"))
                .andExpect(redirectedUrl("/walk/course/10/chat"))
                .andExpect(flash().attribute("error", "정원이 찼습니다."));
    }

    @Test
    void wrongCourseDoesNotJoin() throws Exception {
        doThrow(new IllegalArgumentException("다른 코스입니다.")).when(service).join(10L, 20L, 1L);
        mvc.perform(post("/walk/course/10/chat/20/join"))
                .andExpect(redirectedUrl("/walk/course/10/chat"));
        verify(service).join(10L, 20L, 1L);
    }

    @Test
    void roomRendersParticipantsAndHostExitWarning() throws Exception {
        when(service.findDetail(10L, 20L, 1L)).thenReturn(new ChatRoomDetailResponse(course,
                new ChatRoomResponse(20L, "새 모임", "민서", 1L, true, 1, true),
                List.of(new ChatMemberResponse(1L, "민서"))));
        mvc.perform(get("/walk/course/10/chat/20"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("방장이 나가면 채팅방이 종료됩니다")))
                .andExpect(content().string(containsString("메시지 기능 준비 중")))
                .andExpect(content().string(containsString("/walk/course/10/chat/20/leave")));
    }

    @Test
    void closedRoomRedirectsToList() throws Exception {
        when(service.findDetail(10L, 20L, 1L)).thenThrow(new IllegalStateException("종료된 채팅방입니다."));
        mvc.perform(get("/walk/course/10/chat/20"))
                .andExpect(redirectedUrl("/walk/course/10/chat"))
                .andExpect(flash().attribute("error", "종료된 채팅방입니다."));
    }

    @Test
    void leaveCallsServiceAndReturnsToList() throws Exception {
        mvc.perform(post("/walk/course/10/chat/20/leave"))
                .andExpect(redirectedUrl("/walk/course/10/chat"));
        verify(service).leave(10L, 20L, 1L);
    }

    @Test
    void missingCourseReturnsNotFound() throws Exception {
        when(service.findCourse(10L)).thenThrow(new IllegalArgumentException("존재하지 않는 산책 코스입니다."));
        mvc.perform(get("/walk/course/10/chat")).andExpect(status().isNotFound());
    }

    @Test
    void anonymousUserCannotCreate() throws Exception {
        loginMember = null;
        mvc.perform(post("/walk/course/10/chat").param("title", "새 모임"))
                .andExpect(redirectedUrl("/member/sign-in"));
        verifyNoInteractions(service);
    }
}
