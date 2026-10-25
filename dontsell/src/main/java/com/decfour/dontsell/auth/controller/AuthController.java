package com.decfour.dontsell.auth.controller;

import com.decfour.dontsell.auth.dto.SignInRequest;
import com.decfour.dontsell.auth.dto.SignInResponse;
import com.decfour.dontsell.auth.dto.SignUpRequest;
import com.decfour.dontsell.auth.dto.SignUpResponse;
import com.decfour.dontsell.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<SignUpResponse> signUp(
            @Valid @RequestBody SignUpRequest request
    ) {
        SignUpResponse response = authService.signUp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SignInResponse> signIn(
            @Valid @RequestBody SignInRequest request
    ) {
        String accessToken = authService.signIn(
                request.email(),
                request.password()
        );

        return ResponseEntity.ok(new SignInResponse(accessToken));
    }

    @GetMapping("/me")
    public ResponseEntity<String> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(jwt.getSubject());
    }
}
