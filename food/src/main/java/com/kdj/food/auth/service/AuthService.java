package com.kdj.food.auth.service;

import com.kdj.food.auth.entity.RefreshToken;
import com.kdj.food.auth.repository.RefreshTokenRepository;
import com.kdj.food.entity.Member;
import com.kdj.food.repository.MemberRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final Duration ACCESS_LIFETIME = Duration.ofMinutes(15);
    public static final Duration REFRESH_LIFETIME = Duration.ofDays(7);
    public static final String JWT_ISSUER = "foodlab";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final MemberRepository members;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwords;
    private final JwtEncoder jwtEncoder;
    private final String dummyHash;

    public AuthService(
            MemberRepository members,
            RefreshTokenRepository refreshTokens,
            PasswordEncoder passwords,
            JwtEncoder jwtEncoder
    ) {
        this.members = members;
        this.refreshTokens = refreshTokens;
        this.passwords = passwords;
        this.jwtEncoder = jwtEncoder;
        this.dummyHash = passwords.encode(newRefreshToken());
    }

    @Transactional
    public TokenPair signIn(String email, String password) {
        if (email == null || password == null || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        Member member = members.findByEmail(MemberEmail.normalize(email)).orElse(null);
        String hash = member == null ? dummyHash : member.getPassword();

        if (!passwords.matches(password, hash) || member == null) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        Instant now = Instant.now();
        String refreshToken = newRefreshToken();
        RefreshToken session = new RefreshToken(member, hash(refreshToken), utc(now.plus(REFRESH_LIFETIME)));
        refreshTokens.save(session);

        return new TokenPair(accessToken(member.getId(), now), refreshToken);
    }

    @Transactional
    public TokenPair refresh(String token) {
        if (token == null || token.isBlank()) {
            throw invalidRefreshToken();
        }

        byte[] currentHash = hash(token);
        RefreshToken current = refreshTokens.findByTokenHash(currentHash)
                .orElseThrow(AuthService::invalidRefreshToken);
        Long memberId = current.getMember().getId();

        Instant now = Instant.now();
        String replacement = newRefreshToken();
        int updated = refreshTokens.rotate(
                currentHash,
                hash(replacement),
                utc(now.plus(REFRESH_LIFETIME)),
                utc(now)
        );

        if (updated != 1) {
            throw invalidRefreshToken();
        }

        return new TokenPair(accessToken(memberId, now), replacement);
    }

    @Transactional
    public void signOut(String token) {
        if (token != null && !token.isBlank()) {
            refreshTokens.revoke(hash(token));
        }
    }

    private String accessToken(Long memberId, Instant now) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JWT_ISSUER)
                .subject(memberId.toString())
                .issuedAt(now)
                .expiresAt(now.plus(ACCESS_LIFETIME))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private static String newRefreshToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] hash(String token) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static LocalDateTime utc(Instant time) {
        return LocalDateTime.ofInstant(time, ZoneOffset.UTC);
    }

    private static BadCredentialsException invalidRefreshToken() {
        return new BadCredentialsException("리프레시 토큰이 유효하지 않습니다.");
    }
}
