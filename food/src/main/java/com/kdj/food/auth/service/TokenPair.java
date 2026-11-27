package com.kdj.food.auth.service;

public record TokenPair(String accessToken, String refreshToken) {
    @Override
    public String toString() {
        return "TokenPair[redacted]";
    }
}
