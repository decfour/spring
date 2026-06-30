package com.kdj.commerce.config;

import com.kdj.commerce.web.argumentresolver.LoginMemberArgumentResolver;
import com.kdj.commerce.web.interceptor.LoginCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // 로그인 체크 인터셉터
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor())
                .order(1)
                .addPathPatterns("/shop/**", "/cart/**", "/order/**")
                .excludePathPatterns(
                        "/",
                        "/shop",
                        "/shop/item/*",
                        "/shop/item/*/review",
                        "/shop/item/*/review/[0-9]*",
                        "/member/login",
                        "/member/logout",
                        "/member/register",
                        "/css/**", "/js/**", "/images/**", "/webjars/**", "/*.ico", "/error", "/shop/images/**"
                );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver());
    }
}
