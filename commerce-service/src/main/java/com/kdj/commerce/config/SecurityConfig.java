package com.kdj.commerce.config;

import com.kdj.commerce.web.security.JwtAuthenticationFilter;
import com.kdj.commerce.web.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/walk/course/add",
                                "/walk/course/route",
                                "/walk/course/*/review/add",

                                "/shop/item/*/review/add",

                                "/notice/add"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.GET, "/walk/course/*/pins"
                        ).permitAll()
                        .requestMatchers(
                                "/",
                                "/error",

                                "/member/sign-in",
                                "/member/sign-up",

                                "/walk",
                                "/walk/course/*",
                                "/walk/course/*/review",
                                "/walk/course/*/review/*",

                                "/shop",
                                "/shop/item/*",
                                "/shop/item/*/review",
                                "/shop/item/*/review/*",

                                "/notice",
                                "/notice/*",

                                "/community",
                                "/community/hits",
                                "/community/post/*",

                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/*.ico"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception.authenticationEntryPoint(
                            (request,
                             response,
                             authException
                            ) -> redirectToLogin(request, response)
                        )
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }

    private void redirectToLogin(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        String redirectUrl = request.getRequestURI();
        String query = request.getQueryString();
        if (query != null) {
            redirectUrl += "?" + query;
        }

        String loginUrl = UriComponentsBuilder.fromPath("/member/sign-in")
                .queryParam("redirectURL", redirectUrl)
                .build()
                .encode()
                .toUriString();
        response.sendRedirect(loginUrl);
    }
}

