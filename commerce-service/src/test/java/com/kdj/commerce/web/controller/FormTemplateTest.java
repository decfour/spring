package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.item.DeliveryType;
import com.kdj.commerce.domain.item.ItemType;
import com.kdj.commerce.web.dto.community.PostForm;
import com.kdj.commerce.web.dto.item.ItemForm;
import com.kdj.commerce.web.dto.notice.NoticeForm;
import com.kdj.commerce.web.dto.review.ItemReviewForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FormTemplateTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        var templates = new ClassLoaderTemplateResolver();
        templates.setPrefix("templates/");
        templates.setSuffix(".html");
        templates.setCharacterEncoding("UTF-8");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templates);
        var views = new ThymeleafViewResolver();
        views.setTemplateEngine(engine);
        views.setCharacterEncoding("UTF-8");
        mvc = MockMvcBuilders.standaloneSetup(new FormPreviewController())
                .setViewResolvers(views).build();
    }

    @ParameterizedTest
    @CsvSource({"shop,name", "shop,price", "shop,stock", "shop,description", "shop,deliveryType",
            "community,title", "community,content", "notice,title", "notice,content",
            "review,title", "review,content"})
    void marksOnlyTheRejectedFieldAndKeepsItsErrorMessage(String view, String field) throws Exception {
        for (boolean edit : new boolean[]{false, true}) {
            String body = mvc.perform(get("/form-preview/" + view).param("error", field).param("edit", "" + edit))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
            var input = Pattern.compile("<(?:input|textarea|select)\\b[^>]*\\bname=\"" + field + "\"[^>]*>")
                    .matcher(body);
            assertThat(input.find()).isTrue();
            assertThat(input.group()).containsPattern("class=\"[^\"]*\\bfield-error\\b[^\"]*\"");
            assertThat(Pattern.compile("field-error").matcher(body).results().count()).isEqualTo(1);
            assertThat(body).containsPattern("<div class=\"field-feedback\">\\s*Invalid " + field + "\\s*</div>")
                    .contains("method=\"post\"");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"shop", "community", "notice", "review"})
    void rendersCreateAndEditFormsWithoutSpuriousErrors(String view) throws Exception {
        for (boolean edit : new boolean[]{false, true}) {
            String body = mvc.perform(get("/form-preview/" + view).param("edit", "" + edit))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
            assertThat(body).doesNotContain("field-error", "field-feedback")
                    .containsPattern("<button[^>]*type=\"submit\"[^>]*>\\s*" + (edit ? "수정" : "등록") + "\\s*</button>");
        }
    }

    @Controller
    static class FormPreviewController {
        @GetMapping("/form-preview/{view}")
        ModelAndView form(@PathVariable String view,
                          @RequestParam(defaultValue = "") String error,
                          @RequestParam(defaultValue = "false") boolean edit) {
            Object form;
            String name;
            switch (view) {
                case "shop" -> { var item = new ItemForm(); item.setId(1L); form = item; name = "item"; }
                case "community" -> { var post = new PostForm(); post.setId(1L); form = post; name = "postForm"; }
                case "notice" -> { var notice = new NoticeForm(); notice.setId(1L); form = notice; name = "noticeForm"; }
                case "review" -> { var review = new ItemReviewForm(); review.setId(1L); form = review; name = "reviewForm"; }
                default -> throw new IllegalArgumentException(view);
            }
            var errors = new BeanPropertyBindingResult(form, name);
            if (!error.isEmpty()) errors.rejectValue(error, "invalid", "Invalid " + error);
            return new ModelAndView(view + "/form")
                    .addObject(name, form)
                    .addObject(BindingResult.MODEL_KEY_PREFIX + name, errors)
                    .addObject("isEdit", edit)
                    .addObject("itemId", 10L)
                    .addObject("itemTypes", ItemType.values())
                    .addObject("deliveryTypes", DeliveryType.values());
        }
    }
}
