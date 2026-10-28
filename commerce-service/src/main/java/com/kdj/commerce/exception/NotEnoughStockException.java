package com.kdj.commerce.exception;

public class NotEnoughStockException extends BusinessConflictException {
    public NotEnoughStockException(String message) {
        super(message);
    }
}
