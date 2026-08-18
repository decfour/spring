package com.kdj.commerce.config;

import com.kdj.commerce.web.security.JwtAuthenticationFilter;
import com.kdj.commerce.web.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // REST API 및 세션 미사용(Stateless) 설정
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 접근 권한 제어
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/walk/course/add",
                                "/walk/course/*/delete",
                                "/walk/course/route",
                                "/walk/course/*/like",
                                "/walk/course/*/tag",

                                "/shop/add",
                                "/shop/item/*/review/add",

                                "/community/add",

                                "/notice/add"

                        ).authenticated()
                        .requestMatchers(
                                "/",

                                "/member/login",
                                "/member/register",

                                "/walk",
                                "/walk/course/*",

                                "/shop",
                                "/shop/item/*",
                                "/shop/item/*/review",
                                "/shop/item/*/review/*",

                                "/notice",
                                "/notice/*",

                                "/community",
                                "/community/hits",
                                "/community/post/*",

                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/*.ico",
                                "/error",
                                "/images/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            System.out.println("비로그인 제한 : " + request.getRequestURI());

                            response.sendRedirect("/member/login");
                        })
                )

                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
