package com.kdj.food.service;

import com.kdj.food.dto.DishMaterialInput;
import com.kdj.food.entity.Food;
import com.kdj.food.entity.FoodMaterial;
import com.kdj.food.entity.Material;
import com.kdj.food.repository.FoodMaterialRepository;
import com.kdj.food.repository.MaterialRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
class FoodMaterialService {
    private static final BigDecimal MAX_QUANTITY = new BigDecimal("9999999.999");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);

    private final FoodMaterialRepository links;
    private final MaterialRepository materials;

    FoodMaterialService(FoodMaterialRepository links, MaterialRepository materials) {
        this.links = links;
        this.materials = materials;
    }

    void replace(Food food, List<DishMaterialInput> inputs) {
        List<FoodMaterial> replacements = new ArrayList<>();
        Set<Long> materialIds = new HashSet<>();

        for (DishMaterialInput input : inputs) {
            validate(input);
            Material material = resolve(input);
            if (!materialIds.add(material.getId())) {
                throw new IllegalArgumentException("같은 재료를 두 번 등록할 수 없습니다.");
            }

            replacements.add(new FoodMaterial(food, material, input.quantity(), input.unit().strip()));
        }

        links.deleteLinks(food.getId());
        links.saveAll(replacements);
    }

    private Material resolve(DishMaterialInput input) {
        if (input.materialId() != null) {
            return materials.findById(input.materialId())
                    .orElseThrow(() -> new EntityNotFoundException("재료를 찾을 수 없습니다."));
        }

        String name = WHITESPACE.matcher(input.name()).replaceAll(" ").strip();
        String normalizedName = name.toLowerCase(Locale.ROOT);
        if (name.isEmpty() || name.length() > 100 || normalizedName.length() > 100) {
            throw new IllegalArgumentException("재료 이름은 1~100자여야 합니다.");
        }

        materials.insertIfAbsent(name, normalizedName);
        return materials.findByNormalizedNameForUpdate(normalizedName).orElseThrow();
    }

    private static void validate(DishMaterialInput input) {
        if (input == null || (input.materialId() == null) == (input.name() == null)) {
            throw new IllegalArgumentException("재료 ID 또는 이름 중 하나를 입력해주세요.");
        }
        if (input.unit() == null || input.unit().isBlank() || input.unit().strip().length() > 20) {
            throw new IllegalArgumentException("재료 단위는 1~20자여야 합니다.");
        }

        BigDecimal quantity = input.quantity();
        if (quantity != null && (quantity.signum() <= 0 || quantity.compareTo(MAX_QUANTITY) > 0
                || quantity.stripTrailingZeros().scale() > 3)) {
            throw new IllegalArgumentException("재료 수량은 0보다 크고 소수점 이하 최대 3자리여야 합니다.");
        }
    }
}
