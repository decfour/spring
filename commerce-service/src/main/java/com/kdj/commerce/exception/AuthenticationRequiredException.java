package com.kdj.commerce.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationRequiredException extends BusinessException {
    public AuthenticationRequiredException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
