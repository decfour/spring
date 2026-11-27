package com.kdj.food.repository;

public interface FoodScores {
    Long getId();
    String getName();
    String getImageKey();
    Double getEffort();
    Double getTaste();
    Double getValue();
    Double getEfficiency();
    long getReviewCount();
}
