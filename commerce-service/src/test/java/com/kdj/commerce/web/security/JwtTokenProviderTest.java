package com.kdj.commerce.web.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Date;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {
    private static final String SIGNING_SECRET = "test-only-jwt-signing-secret-123456789";
    private final JwtTokenProvider provider = new JwtTokenProvider();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(provider, "secretKey", SIGNING_SECRET);
        ReflectionTestUtils.setField(provider, "expirationPeriod", 1_800_000L);
        provider.initializeSigningKeyAndParser();
    }

    @Test
    void extractsMemberIdFromIssuedToken() {
        assertThat(provider.verifyAndReadMemberId(provider.issueToken(42L))).contains(42L);
    }

    @Test
    void rejectsLegacyEmailSubject() {
        String token = signToken("dontpanic", "member@example.com", new Date(System.currentTimeMillis() + 60_000));

        assertThat(provider.verifyAndReadMemberId(token)).isEmpty();
    }

    @Test
    void rejectsExpiredToken() {
        String token = signToken("dontpanic", "42", new Date(System.currentTimeMillis() - 60_000));

        assertThat(provider.verifyAndReadMemberId(token)).isEmpty();
    }

    @Test
    void rejectsTokenWithoutIssuer() {
        String token = signToken(null, "42", new Date(System.currentTimeMillis() + 60_000));

        assertThat(provider.verifyAndReadMemberId(token)).isEmpty();
    }

    @Test
    void rejectsTokenWithDifferentIssuer() {
        String token = signToken("another-issuer", "42", new Date(System.currentTimeMillis() + 60_000));

        assertThat(provider.verifyAndReadMemberId(token)).isEmpty();
    }

    private String signToken(String issuer, String subject, Date expiration) {
        byte[] signingKey = Base64.getEncoder().encode(SIGNING_SECRET.getBytes(UTF_8));
        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(subject)
                .setExpiration(expiration)
                .signWith(Keys.hmacShaKeyFor(signingKey), SignatureAlgorithm.HS256)
                .compact();
    }
}
