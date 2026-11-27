package com.kdj.food.service;

import com.kdj.food.dto.DishMaterial;
import com.kdj.food.dto.DishMaterialInput;
import com.kdj.food.entity.Food;
import com.kdj.food.entity.Member;
import com.kdj.food.repository.FoodMaterialRepository;
import com.kdj.food.repository.FoodRepository;
import com.kdj.food.repository.FoodScores;
import com.kdj.food.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FoodService {
    private final FoodRepository foods;
    private final MemberRepository members;
    private final FoodMaterialRepository foodMaterials;
    private final FoodMaterialService materialWriter;

    public FoodService(
            FoodRepository foods,
            MemberRepository members,
            FoodMaterialRepository foodMaterials,
            FoodMaterialService materialWriter
    ) {
        this.foods = foods;
        this.members = members;
        this.foodMaterials = foodMaterials;
        this.materialWriter = materialWriter;
    }

    public Page<Summary> search(String name, FoodSort sort, Pageable page) {
        Pageable sortedPage = PageRequest.of(
                page.getPageNumber(),
                page.getPageSize(),
                Sort.by(Sort.Direction.DESC, sort.property, "id")
        );
        String query = name == null ? "" : name.strip();

        return foods.search(query, sortedPage).map(Summary::from);
    }

    public Page<Summary> findByMember(Long memberId, Pageable page) {
        return foods.findSummariesByMember(memberId, page).map(Summary::from);
    }

    public Detail get(Long foodId) {
        Food food = findFood(foodId);
        Summary summary = Summary.from(foods.findSummary(foodId)
                .orElseThrow(FoodService::foodNotFound));
        Member author = food.getMember();

        List<DishMaterial> materials = foodMaterials.findAllByFood_IdOrderByIdAsc(foodId).stream()
                .map(DishMaterial::from)
                .toList();

        return new Detail(
                summary,
                food.getDescription(),
                author.getId(),
                author.getName(),
                materials
        );
    }

    @Transactional
    public Long create(
            Long memberId,
            String name,
            String description,
            String imageKey
    ) {
        return create(memberId, name, description, imageKey, null);
    }

    @Transactional
    public Long create(
            Long memberId,
            String name,
            String description,
            String imageKey,
            List<DishMaterialInput> materials
    ) {
        validateDetails(name, description, imageKey);

        Member member = members.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        Food food = foods.save(new Food(member, name, description, imageKey));
        if (materials != null && !materials.isEmpty()) {
            materialWriter.replace(food, materials);
        }

        return food.getId();
    }

    @Transactional
    public void update(
            Long foodId,
            Long memberId,
            String name,
            String description,
            String imageKey
    ) {
        update(foodId, memberId, name, description, imageKey, null);
    }

    @Transactional
    public void update(
            Long foodId,
            Long memberId,
            String name,
            String description,
            String imageKey,
            List<DishMaterialInput> materials
    ) {
        validateDetails(name, description, imageKey);

        Food food = foods.findForUpdate(foodId).orElseThrow(FoodService::foodNotFound);
        requireOwner(food, memberId);
        food.updateDetails(name, description, imageKey);

        if (materials != null) {
            materialWriter.replace(food, materials);
        }
    }

    @Transactional
    public void delete(Long foodId, Long memberId) {
        Food food = findFood(foodId);
        requireOwner(food, memberId);
        foods.delete(food);
    }

    private static void requireOwner(Food food, Long memberId) {
        if (!food.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("작성자만 요리를 수정하거나 삭제할 수 있습니다.");
        }
    }

    private Food findFood(Long foodId) {
        return foods.findById(foodId).orElseThrow(FoodService::foodNotFound);
    }

    private static EntityNotFoundException foodNotFound() {
        return new EntityNotFoundException("요리를 찾을 수 없습니다.");
    }

    private static void validateDetails(String name, String description, String imageKey) {
        if (name == null || name.isBlank() || name.length() > 100
                || description == null || description.isBlank()
                || imageKey == null || imageKey.isBlank() || imageKey.length() > 255) {
            throw new IllegalArgumentException("요리 이름, 소개, 대표 이미지를 확인해주세요.");
        }
        if (description.getBytes(StandardCharsets.UTF_8).length > 65_535) {
            throw new IllegalArgumentException("요리 소개는 UTF-8 기준 65,535바이트 이하여야 합니다.");
        }
    }

    public enum FoodSort {
        LATEST("createdAt"), EFFORT("effort"), TASTE("taste"), VALUE("value"), EFFICIENCY("efficiency");

        private final String property;

        FoodSort(String property) {
            this.property = property;
        }
    }

    public record Summary(
            Long id,
            String name,
            String imageKey,
            Double effort,
            Double taste,
            Double value,
            Double efficiency,
            long reviewCount
    ) {
        private static Summary from(FoodScores scores) {
            return new Summary(
                    scores.getId(),
                    scores.getName(),
                    scores.getImageKey(),
                    scores.getEffort(),
                    scores.getTaste(),
                    scores.getValue(),
                    scores.getEfficiency(),
                    scores.getReviewCount()
            );
        }
    }

    public record Detail(
            Summary summary,
            String description,
            Long authorId,
            String authorName,
            List<DishMaterial> materials
    ) {
    }
}
