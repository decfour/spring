package com.kdj.commerce.exception;

public class DuplicateMemberException extends IllegalStateException {
    public DuplicateMemberException(String message) {
        super(message);
    }
}