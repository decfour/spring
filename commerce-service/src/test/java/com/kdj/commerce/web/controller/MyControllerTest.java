package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.MemberService;
import com.kdj.commerce.service.MyService;
import com.kdj.commerce.service.ItemService;
import com.kdj.commerce.service.ItemReviewService;
import com.kdj.commerce.service.PurchaseService;
import com.kdj.commerce.service.WalkCourseService;
import com.kdj.commerce.web.security.JwtTokenProvider;
import com.kdj.commerce.web.argumentresolver.LoginMemberArgumentResolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MyControllerTest {
    @ParameterizedTest
    @CsvSource({
            "/member/my-page,myPage,member,none",
            "/member/my-course,myCourse,myCourses,my-course",
            "/member/my-shop,myShop,myItems,item",
            "/member/my-review,myShop,myReviews,review",
            "/member/my-order,myShop,purchases,order"
    })
    void movedRoutesKeepTheirViewsAndQueryTheAuthenticatedMembersPage(String url, String view, String attribute, String tab) throws Exception {
        var members = mock(MemberService.class);
        var member = mock(Member.class);
        when(member.getId()).thenReturn(42L);
        when(members.findByEmail("me@example.com")).thenReturn(member);
        var items = mock(ItemService.class);
        var reviews = mock(ItemReviewService.class);
        var purchases = mock(PurchaseService.class);
        var courses = mock(WalkCourseService.class);
        var expectedPage = org.springframework.data.domain.PageRequest.of(2, 7,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
        when(items.findByCreatorId(expectedPage, 42L)).thenReturn(Page.empty(expectedPage));
        when(reviews.findByCreatorId(expectedPage, 42L)).thenReturn(Page.empty(expectedPage));
        when(purchases.findByMemberId(expectedPage, 42L)).thenReturn(Page.empty(expectedPage));
        when(courses.findByCreatorId(expectedPage, 42L)).thenReturn(Page.empty(expectedPage));
        var mvc = MockMvcBuilders.standaloneSetup(
                        new MyController(mock(MyService.class), items, reviews, purchases, courses),
                        new MemberController(members, mock(JwtTokenProvider.class)))
                .setCustomArgumentResolvers(new LoginMemberArgumentResolver(members), new PageableHandlerMethodArgumentResolver()).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("me@example.com", "", List.of()));
        try {
            var result = mvc.perform(get(url).param("page", "2")).andExpect(status().isOk())
                    .andExpect(view().name("member/" + view)).andExpect(model().attributeExists(attribute));
            if (view.equals("myPage")) {
                result.andExpect(model().attribute("member", member));
            } else {
                result.andExpect(model().attribute("tab", tab)).andExpect(model().attributeDoesNotExist("member"));
                switch (attribute) {
                    case "myCourses" -> verify(courses).findByCreatorId(expectedPage, 42L);
                    case "myItems" -> verify(items).findByCreatorId(expectedPage, 42L);
                    case "myReviews" -> verify(reviews).findByCreatorId(expectedPage, 42L);
                    default -> verify(purchases).findByMemberId(expectedPage, 42L);
                }
            }
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @ParameterizedTest
    @CsvSource({
            "/member/my-pin,Course,my-pin,/member/my-pin",
            "/member/my-chat,Course,my-chat,/member/my-chat",
            "/member/my-course-review,Course,my-course-review,/member/my-course-review",
            "/member/my-talk,Talk,post,/member/my-talk?tab=post",
            "/member/my-talk?tab=comment,Talk,comment,/member/my-talk?tab=comment",
            "/member/my-talk?tab=unknown,Talk,post,/member/my-talk?tab=post"
    })
    void authenticatedActivityRoutesKeepTheirSectionAndPagingTarget(String url, String section, String tab, String target) throws Exception {
        var service = mock(MyService.class);
        var members = mock(MemberService.class);
        var member = mock(Member.class);
        when(member.getId()).thenReturn(42L);
        when(members.findByEmail("me@example.com")).thenReturn(member);
        when(service.pins(eq(42L), any())).thenReturn(Page.empty());
        when(service.chats(eq(42L), any())).thenReturn(Page.empty());
        when(service.reviews(eq(42L), any())).thenReturn(Page.empty());
        when(service.posts(eq(42L), any())).thenReturn(Page.empty());
        when(service.comments(eq(42L), any())).thenReturn(Page.empty());
        var mvc = MockMvcBuilders.standaloneSetup(new MyController(service, mock(ItemService.class),
                        mock(ItemReviewService.class), mock(PurchaseService.class), mock(WalkCourseService.class)))
                .setCustomArgumentResolvers(new LoginMemberArgumentResolver(members), new PageableHandlerMethodArgumentResolver()).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("me@example.com", "", List.of()));
        try {
            mvc.perform(get(url)).andExpect(status().isOk()).andExpect(view().name(section.equals("Course") ? "member/myCourse" : "member/myTalk"))
                    .andExpect(model().attribute("tab", tab))
                    .andExpect(model().attribute("listUrl", target)).andExpect(model().attributeExists("entries"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
