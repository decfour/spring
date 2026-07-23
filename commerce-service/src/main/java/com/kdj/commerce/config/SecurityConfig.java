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
                // REST API 위한 기본 보안 비활성화
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 세션을 생성하거나 사용하지 않음 (Stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 접근 권한 제어
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/shop",
                                "/shop/item/*",
                                "/shop/item/*/review",
                                "/shop/item/*/review/[0-9]*",
                                "/member/login",
                                "/member/logout",
                                "/member/register",
                                "/notice",
                                "/notice/[0-9]*",
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/*.ico", "/error", "/shop/images/**"
                        ).permitAll()

                        .requestMatchers("/shop/**", "/cart/**", "/order/**", "/member/my-page", "/notice**").authenticated()

                        .anyRequest().permitAll()
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
