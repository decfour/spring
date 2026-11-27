package com.kdj.food.config;

import com.kdj.food.controller.FoodController;
import com.kdj.food.common.web.ApiError;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper json) throws Exception {
        AuthenticationEntryPoint entryPoint = jsonAuthenticationEntryPoint(json);
        return http
                // Cookie-backed auth requests require a custom header; cross-origin requests have no CORS grant.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.GET, FoodController.DISHES_PATH,
                                FoodController.DISHES_PATH + "/*",
                                FoodController.DISHES_PATH + "/*/reviews").permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/signUp",
                                "/api/v1/auth/signIn",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/signOut"
                        ).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors.authenticationEntryPoint(entryPoint))
                .oauth2ResourceServer(server -> server.jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(entryPoint))
                .build();
    }

    private AuthenticationEntryPoint jsonAuthenticationEntryPoint(ObjectMapper json) {
        var bearer = new BearerTokenAuthenticationEntryPoint();

        return (request, response, exception) -> {
            bearer.commence(request, response, exception);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            json.writeValue(response.getOutputStream(), new ApiError("UNAUTHORIZED", "유효한 액세스 토큰이 필요합니다."));
        };
    }
}
