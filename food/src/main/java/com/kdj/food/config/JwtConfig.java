package com.kdj.food.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.kdj.food.auth.service.AuthService;
import java.time.Duration;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration(proxyBeanMethods = false)
public class JwtConfig {
    @Bean
    SecretKeySpec jwtSigningKey(@Value("${FOOD_JWT_SECRET}") String secret) {
        byte[] key = Base64.getDecoder().decode(secret);
        if (key.length < 32) {
            throw new IllegalArgumentException(
                    "FOOD_JWT_SECRET must contain at least 32 Base64-encoded bytes"
            );
        }

        return new SecretKeySpec(key, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKeySpec signingKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(signingKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKeySpec signingKey) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(signingKey).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtIssuerValidator(AuthService.JWT_ISSUER),
                new JwtTimestampValidator(Duration.ZERO),
                JwtConfig::validateMemberToken));
        return decoder;
    }

    private static OAuth2TokenValidatorResult validateMemberToken(Jwt jwt) {
        String subject = jwt.getSubject();
        if (jwt.getExpiresAt() != null && subject != null) {
            try {
                if (Long.parseLong(subject) > 0) {
                    return OAuth2TokenValidatorResult.success();
                }
            } catch (NumberFormatException ignored) {
                // Member IDs in tokens must fit into a signed 64-bit integer.
            }
        }
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
    }
}
