package com.kdj.commerce.exception;

import org.springframework.http.HttpStatus;

public class BusinessConflictException extends BusinessException {
    public BusinessConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
