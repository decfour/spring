package com.kdj.food.auth.controller;

import com.kdj.food.auth.service.AuthService;
import com.kdj.food.auth.service.TokenPair;
import com.kdj.food.auth.service.MemberSignUpService;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(AuthController.AUTH_PATH)
public class AuthController {
    static final String AUTH_PATH = "/api/v1/auth";
    private static final String REFRESH_COOKIE = "food_refresh";
    private static final String AUTH_HEADER = "X-FoodLab-Auth";

    private final AuthService authService;
    private final MemberSignUpService signUpService;
    private final boolean secureCookie;

    public AuthController(
            AuthService authService,
            MemberSignUpService signUpService,
            @Value("${FOOD_REFRESH_COOKIE_SECURE:true}") boolean secureCookie
    ) {
        this.authService = authService;
        this.signUpService = signUpService;
        this.secureCookie = secureCookie;
    }

    @PostMapping(path = "/signUp", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignUpRequest request) {
        Long memberId = signUpService.signUp(
                request.email(),
                request.password(),
                request.name()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new SignUpResponse(memberId));
    }

    @PostMapping(path = "/signIn", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccessTokenResponse> signIn(@RequestBody SignInRequest request) {
        TokenPair tokens = authService.signIn(request.email(), request.password());

        return tokenResponse(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(
            @RequestHeader(name = AUTH_HEADER, required = false) String authHeader,
            @CookieValue(name = REFRESH_COOKIE, required = false) String token
    ) {
        requireAuthHeader(authHeader);

        return tokenResponse(authService.refresh(token));
    }

    @PostMapping("/signOut")
    public ResponseEntity<Void> signOut(
            @RequestHeader(name = AUTH_HEADER, required = false) String authHeader,
            @CookieValue(name = REFRESH_COOKIE, required = false) String token
    ) {
        requireAuthHeader(authHeader);
        authService.signOut(token);

        return ResponseEntity
                .noContent()
                .cacheControl(CacheControl.noStore())
                .header(
                        HttpHeaders.SET_COOKIE,
                        refreshCookie("", Duration.ZERO).toString()
                )
                .build();
    }

    private ResponseEntity<AccessTokenResponse> tokenResponse(TokenPair tokens) {
        ResponseCookie cookie = refreshCookie(tokens.refreshToken(), AuthService.REFRESH_LIFETIME);

        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AccessTokenResponse(tokens.accessToken()));
    }

    private ResponseCookie refreshCookie(String token, Duration maxAge) {
        return ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path(AUTH_PATH)
                .maxAge(maxAge)
                .build();
    }

    private static void requireAuthHeader(String authHeader) {
        if (!"1".equals(authHeader)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "인증 요청 헤더가 필요합니다.");
        }
    }

    public record SignUpRequest(String email, String password, String name) {
        @Override
        public String toString() {
            return "SignUpRequest[redacted]";
        }
    }

    public record SignInRequest(String email, String password) {
        @Override
        public String toString() {
            return "SignInRequest[redacted]";
        }
    }

    public record SignUpResponse(Long memberId) {
    }

    public record AccessTokenResponse(String accessToken) {
        @Override
        public String toString() {
            return "AccessTokenResponse[redacted]";
        }
    }
}
