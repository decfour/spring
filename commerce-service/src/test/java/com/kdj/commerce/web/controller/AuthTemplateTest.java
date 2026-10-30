package com.kdj.commerce.web.controller;

import com.kdj.commerce.web.dto.member.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthTemplateTest {
    @ParameterizedTest
    @ValueSource(strings={"in","up","in-error","up-error"})
    void retainsFormBindingsAndErrorsWithinSharedFrame(String view) throws Exception {
        var resolver=new ClassLoaderTemplateResolver();resolver.setPrefix("templates/");resolver.setSuffix(".html");resolver.setCharacterEncoding("UTF-8");
        var engine=new SpringTemplateEngine();engine.setTemplateResolver(resolver);
        var views=new ThymeleafViewResolver();views.setTemplateEngine(engine);views.setCharacterEncoding("UTF-8");
        var mvc=MockMvcBuilders.standaloneSetup(new PreviewController()).setViewResolvers(views).build();
        String html=mvc.perform(get("/auth-preview/"+view)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(html).contains("auth-layout", "auth-page panel", "/css/auth.css", "method=\"post\"");
        if(view.startsWith("in"))assertThat(html).contains("action=\"/member/sign-in\"", "value=\"/walk/course/10\"");
        else assertThat(html).contains("action=\"/member/sign-up\"", "id=\"emailLocal\"", "id=\"emailDomain\"");
        if(view.endsWith("error"))assertThat(html).contains("입력값을 확인해주세요", "필수 입력입니다", "is-invalid");
        Files.createDirectories(Path.of("build/ui-auth"));Files.writeString(Path.of("build/ui-auth",view+".html"),html);
    }
    @Controller
    static class PreviewController {
        @GetMapping("/auth-preview/{view}")
        ModelAndView preview(@PathVariable String view) {
            boolean signIn=view.startsWith("in");
            Object form=signIn?new SignInForm():new SignUpForm();
            if(form instanceof SignUpForm signUp)signUp.setEmail("traveler@example.com");
            String name=signIn?"signInForm":"member";
            var errors=new BeanPropertyBindingResult(form,name);
            if(view.endsWith("error")){errors.reject("invalid","입력값을 확인해주세요");errors.rejectValue(signIn?"signInId":"email","required","필수 입력입니다");}
            return new ModelAndView(signIn?"member/signInForm":"member/signUpForm").addObject(name,form)
                    .addObject(BindingResult.MODEL_KEY_PREFIX+name,errors).addObject("redirectURL","/walk/course/10");
        }
    }
}
