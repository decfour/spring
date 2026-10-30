package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.item.*;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.domain.purchase.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MyTemplateTest {
    @ParameterizedTest
    @ValueSource(strings={"page","course","item","review","purchase","empty-course","empty-item","empty-review","empty-purchase"})
    void rendersSharedFramesAndPreservesActivityDestinations(String view) throws Exception {
        var resolver=new ClassLoaderTemplateResolver();resolver.setPrefix("templates/");resolver.setSuffix(".html");resolver.setCharacterEncoding("UTF-8");
        var engine=new SpringTemplateEngine();engine.setTemplateResolver(resolver);
        var views=new ThymeleafViewResolver();views.setTemplateEngine(engine);views.setCharacterEncoding("UTF-8");
        var mvc=MockMvcBuilders.standaloneSetup(new PreviewController()).setViewResolvers(views).build();
        String html=mvc.perform(get("/my-preview/"+view)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("class=\"theme\"", "/css/my.css", "panel").doesNotContain("surface-card", "btn-cancel");
        if(view.equals("course"))assertThat(html).contains("/walk/course/10");
        if(view.equals("item"))assertThat(html).contains("/shop/item/10", "품절", "판매 중지");
        if(view.equals("review"))assertThat(html).contains("/shop/item/10/review/1");
        if(view.equals("purchase"))assertThat(html).contains("action=\"/order/1/cancel\"", "주문 취소").doesNotContain("action=\"/order/2/cancel\"");
        Files.createDirectories(Path.of("build/ui-my"));Files.writeString(Path.of("build/ui-my",view+".html"),html);
    }
    @Controller
    static class PreviewController {
        @GetMapping("/my-preview/{view}")
        ModelAndView preview(@PathVariable String view) {
            String kind=view.replace("empty-","");String template=switch(kind){case "page"->"myPage";case "course"->"myCourse";case "item"->"myItem";case "review"->"myReview";default->"myPurchase";};
            var item=Item.create("여행길에 읽는 길고 긴 이름의 작은 안내서",6000,0,"설명",true,false,ItemType.BOOK,DeliveryType.STANDARD,1L,null,null);item.setId(10L);
            var closed=Item.create("판매를 마친 작은 여행 안내서",6000,2,"설명",false,false,ItemType.BOOK,DeliveryType.STANDARD,1L,null,null);closed.setId(11L);
            var course=Map.of("id",10L,"title","숲과 강을 따라 천천히 걷는 길고 긴 이름의 산책 코스","distance",1200,"createdAt",LocalDateTime.of(2026,10,31,10,0));
            var review=Map.of("id",1L,"title","다음 여행에도 함께하고 싶은 작은 안내서","item",item);
            var purchaseItem=mock(PurchaseItem.class);when(purchaseItem.getItem()).thenReturn(item);
            var order=mock(Purchase.class);when(order.getId()).thenReturn(1L);when(order.getStatus()).thenReturn(PurchaseStatus.ORDER);when(order.getPurchaseItems()).thenReturn(List.of(purchaseItem,purchaseItem));when(order.getTotalPrice()).thenReturn(12000);when(order.getCreatedAt()).thenReturn(LocalDateTime.of(2026,10,31,10,0));
            var cancelled=mock(Purchase.class);when(cancelled.getId()).thenReturn(2L);when(cancelled.getStatus()).thenReturn(PurchaseStatus.CANCEL);when(cancelled.getPurchaseItems()).thenReturn(List.of(purchaseItem));when(cancelled.getTotalPrice()).thenReturn(6000);when(cancelled.getCreatedAt()).thenReturn(LocalDateTime.of(2026,10,30,10,0));
            var result=new ModelAndView("member/"+template).addObject("member",Map.of("name","여행자","memberType",MemberType.USER,"signInId","traveler","email","traveler.with.a.long.email.address@example.com"));
            boolean empty=view.startsWith("empty-");
            return result.addObject("myCourses",empty?Page.empty():new PageImpl<>(List.of(course,course,course),PageRequest.of(1,7),35))
                    .addObject("myItems",empty?Page.empty():new PageImpl<>(List.of(item,closed),PageRequest.of(1,7),35))
                    .addObject("myReviews",empty?Page.empty():new PageImpl<>(List.of(review,review),PageRequest.of(1,7),35))
                    .addObject("purchases",empty?Page.empty():new PageImpl<>(List.of(order,cancelled),PageRequest.of(1,7),35));
        }
    }
}
