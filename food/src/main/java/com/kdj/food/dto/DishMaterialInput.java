package com.kdj.food.dto;

import java.math.BigDecimal;

public record DishMaterialInput(Long materialId, String name, BigDecimal quantity, String unit) {
}
