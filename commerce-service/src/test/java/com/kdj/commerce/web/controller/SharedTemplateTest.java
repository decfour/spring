package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.cart.Cart;
import com.kdj.commerce.domain.cart.CartItem;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.web.dto.purchase.PurchaseItemForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
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
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class SharedTemplateTest {
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        var views = new ThymeleafViewResolver();
        views.setTemplateEngine(engine);
        views.setCharacterEncoding("UTF-8");
        mvc = MockMvcBuilders.standaloneSetup(new PreviewController(), new FormTemplateTest.FormPreviewController())
                .setViewResolvers(views).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"community/list", "community/hit", "community/detail", "notice/list", "review/list",
            "shop/list", "member/myItem", "member/myReview", "member/myCourse", "member/myPurchase", "walk/hit", "chat/home"})
    void paginationRendersAcrossAllConsumers(String view) throws Exception {
        String body = render(view, 0, 100);
        assertThat(body).contains("aria-label=\"페이지 탐색\"", "aria-current=\"page\"")
                .containsPattern("<span[^>]*aria-label=\"이전 페이지\"[^>]*aria-disabled=\"true\"[^>]*>‹</span>")
                .doesNotContain("href=\"\"")
                .doesNotContain("page=-1", "class=\"pagination");
        assertThat(body.split("class=\"btn-page", -1).length - 1).isEqualTo(7);
        if (view.equals("chat/home")) {
            assertThat(body).contains("/walk/course/7/chat?mine=true&amp;page=1");
        }
        if (view.equals("review/list")) {
            assertThat(body).contains("/shop/item/10/review?page=1");
        }
        savePreview(view, body);
    }

    @ParameterizedTest
    @CsvSource({"0,0", "0,1", "5,100", "9,100"})
    void paginationHandlesEmptySingleMiddleAndLastPage(int page, int total) throws Exception {
        String body = render("notice/list", page, total);
        if (total <= 1) {
            assertThat(body).doesNotContain("class=\"pager\"");
        } else {
            assertThat(body.split("class=\"btn-page", -1).length - 1).isEqualTo(7);
            assertThat(body).contains(">" + (page + 1) + "</a>");
            if (page == 9) {
                assertThat(body).doesNotContain("href=\"/notice?page=10\"")
                        .containsPattern("<span[^>]*aria-label=\"다음 페이지\"[^>]*aria-disabled=\"true\"[^>]*>›</span>");
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"cart/cartList", "purchase/purchaseForm"})
    void summaryKeepsAmountAndOrderAction(String view) throws Exception {
        String body = render(view, 0, 0);
        assertThat(body).contains("class=\"summary-total\"", "class=\"summary-value\">12,000원</strong>")
                .contains(view.startsWith("cart") ? "/order/cart" : "/order/create");
        savePreview(view, body);
    }

    @ParameterizedTest
    @CsvSource({"community,/community/post/1", "notice,/notice/1", "review,/shop/item/10/review/1"})
    void sharedFormPreservesEditCancelDestination(String view, String destination) throws Exception {
        String body = mvc.perform(get("/form-preview/" + view).param("edit", "true").param("error", "title"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(body).contains("href=\"" + destination + "\"", "aria-invalid=\"true\"", "Invalid title");
        savePreview(view + "/form", body);
    }

    private String render(String view, int page, int total) throws Exception {
        return mvc.perform(get("/shared-preview/" + view).param("page", "" + page).param("total", "" + total))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private void savePreview(String view, String body) throws Exception {
        Path directory = Path.of("build", "ui-reuse");
        Files.createDirectories(directory);
        Files.writeString(directory.resolve(view.replace('/', '-') + ".html"), body);
    }

    @Controller
    static class PreviewController {
        @GetMapping("/shared-preview/{area}/{name}")
        ModelAndView preview(@PathVariable String area, @PathVariable String name,
                             @RequestParam int page, @RequestParam int total) {
            var result = new ModelAndView(area + "/" + name);
            var data = new PageImpl<>(List.of(), PageRequest.of(page, 10), total);
            for (String key : List.of("posts", "comments", "notices", "reviews", "items", "myItems",
                    "myReviews", "myCourses", "purchases", "courses", "rooms")) {
                result.addObject(key, data);
            }
            Item item = new Item();
            item.setId(10L);
            item.setName("산책용 물병");
            item.setPrice(6000);
            result.addObject("cartItems", List.of(CartItem.create(mock(Cart.class), item, 2)))
                    .addObject("purchaseItems", List.of(new PurchaseItemForm("산책용 물병", 6000, 2)))
                    .addObject("totalPrice", 12000).addObject("purchaseType", "ONE")
                    .addObject("quantity", 2).addObject("itemId", 10L).addObject("mine", true)
                    .addObject("course", Map.of("id", 7L, "title", "산책 코스", "distance", 1200, "duration", 900))
                    .addObject("post", Map.of("id", 1L, "title", "산책 이야기", "content", "본문",
                            "createdAt", LocalDateTime.now(), "viewCount", 1, "likeCount", 0, "creator", Map.of("name", "작성자")));
            return result;
        }
    }
}
