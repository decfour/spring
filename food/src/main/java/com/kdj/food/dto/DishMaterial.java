package com.kdj.food.dto;

import com.kdj.food.entity.FoodMaterial;
import com.kdj.food.entity.Material;
import java.math.BigDecimal;

public record DishMaterial(
        Long materialId,
        String name,
        BigDecimal quantity,
        String unit
) {
    public static DishMaterial from(FoodMaterial foodMaterial) {
        Material material = foodMaterial.getMaterial();

        return new DishMaterial(
                material.getId(),
                material.getName(),
                foodMaterial.getQuantity(),
                foodMaterial.getUnit()
        );
    }
}
