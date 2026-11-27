package com.kdj.food.service;

public class DuplicateReviewException extends RuntimeException {
    public DuplicateReviewException() {
        super("이미 평가한 요리입니다.");
    }
}
