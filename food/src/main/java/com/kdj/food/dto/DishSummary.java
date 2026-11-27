package com.kdj.food.dto;

import com.kdj.food.service.FoodService;

public record DishSummary(
        Long id,
        String name,
        String imageId,
        Double effort,
        Double taste,
        Double value,
        Double efficiency,
        long reviewCount
) {
    public static DishSummary from(FoodService.Summary summary) {
        return new DishSummary(
                summary.id(),
                summary.name(),
                summary.imageKey(),
                summary.effort(),
                summary.taste(),
                summary.value(),
                summary.efficiency(),
                summary.reviewCount()
        );
    }
}
