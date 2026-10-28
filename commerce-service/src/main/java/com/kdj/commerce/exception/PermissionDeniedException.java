package com.kdj.commerce.exception;

import org.springframework.http.HttpStatus;

public class PermissionDeniedException extends BusinessException {
    public PermissionDeniedException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
