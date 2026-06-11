package com.kdj.commerce.config;

import com.kdj.commerce.web.interceptor.LoginCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 회원 확인 인터셉터
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor())
                .order(1)
                .addPathPatterns("/shop/**", "/cart/**", "/order/**")
                .excludePathPatterns(
                        "/",
                        "/shop",
                        "/shop/item/*",
                        "/member/login",
                        "/member/logout",
                        "/member/register",
                        "/css/**", "/js/**", "/images/**", "/webjars/**", "/*.ico", "/error", "/shop/images/**"
                );
    }
}
