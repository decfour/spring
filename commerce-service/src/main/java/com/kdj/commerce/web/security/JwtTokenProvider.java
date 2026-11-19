package com.kdj.commerce.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtTokenProvider {
    private static final String ISSUER = "dontpanic";

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-period}")
    private long expirationPeriod;

    private Key signingKey;
    private JwtParser jwtParser;

    @PostConstruct
    protected void initializeSigningKeyAndParser() {
        String encodedKey = Base64.getEncoder()
                .encodeToString(secretKey.getBytes());
        signingKey = Keys.hmacShaKeyFor(encodedKey.getBytes());

        jwtParser = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .requireIssuer(ISSUER)
                .build();
    }

    // JWT 발행
    public String issueToken(long memberId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationPeriod);

        return Jwts.builder()
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .setIssuer(ISSUER)
                .setSubject(Long.toString(memberId))
                .setIssuedAt(now)
                .setExpiration(expiration)
                .compact();
    }

    // JWT 검증
    public Optional<Long> verifyAndReadMemberId(String token) {
        try {
            Claims claims = jwtParser.parseClaimsJws(token).getBody();
            if (claims.getExpiration() == null) {
                return Optional.empty();
            }

            long memberId = Long.parseLong(claims.getSubject());

            return memberId > 0 ? Optional.of(memberId) : Optional.empty();
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
