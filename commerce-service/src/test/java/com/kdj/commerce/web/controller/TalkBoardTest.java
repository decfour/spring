package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.community.Post;
import com.kdj.commerce.domain.notice.Notice;
import com.kdj.commerce.service.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.stream.IntStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TalkBoardTest {
    @ParameterizedTest
    @ValueSource(strings={"/community","/community/hits"})
    void paginatesThreeNoticesAndTenPostsIndependently(String url) throws Exception {
        var posts=mock(PostService.class);var notices=mock(NoticeService.class);
        var postData=IntStream.range(0,10).mapToObj(i->{var p=Post.create("함께 걷는 이야기 "+i,"본문",null);ReflectionTestUtils.setField(p,"id",(long)i+1);return p;}).toList();
        var noticeData=IntStream.range(0,3).mapToObj(i->{var n=Notice.create("산책자를 위한 공지 "+i,"내용");ReflectionTestUtils.setField(n,"id",(long)i+1);return n;}).toList();
        when(posts.findAll(any(Pageable.class))).thenAnswer(a->new PageImpl<>(postData,a.getArgument(0),40));
        when(posts.findHit(any(Pageable.class))).thenAnswer(a->new PageImpl<>(postData,a.getArgument(0),40));
        when(notices.findAll(any(Pageable.class))).thenAnswer(a->new PageImpl<>(noticeData,a.getArgument(0),20));
        var resolver=new ClassLoaderTemplateResolver();resolver.setPrefix("templates/");resolver.setSuffix(".html");resolver.setCharacterEncoding("UTF-8");
        var engine=new SpringTemplateEngine();engine.setTemplateResolver(resolver);
        var views=new ThymeleafViewResolver();views.setTemplateEngine(engine);views.setCharacterEncoding("UTF-8");
        var mvc=MockMvcBuilders.standaloneSetup(new PostController(posts,mock(PostCommentService.class),notices))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()).setViewResolvers(views).build();
        var response=mvc.perform(get(url).param("page","1").param("noticePage","2")).andExpect(status().isOk()).andReturn();
        String html=response.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains(url+"?page=1&amp;size=10&amp;noticePage=3",url+"?noticePage=2&amp;size=10&amp;page=2");
        assertThat(html.split("class=\"board-entry\"",-1)).hasSize(14);
        verify(notices).findAll(argThat(p->p.getPageNumber()==2&&p.getPageSize()==3&&p.getSort().getOrderFor("id").isDescending()));
        if(url.endsWith("hits"))verify(posts).findHit(argThat(p->p.getPageNumber()==1&&p.getPageSize()==10));
        else verify(posts).findAll(argThat(p->p.getPageNumber()==1&&p.getPageSize()==10));
        Files.createDirectories(Path.of("build/ui-talk"));Files.writeString(Path.of("build/ui-talk",url.endsWith("hits")?"hit.html":"list.html"),html);
    }
}
