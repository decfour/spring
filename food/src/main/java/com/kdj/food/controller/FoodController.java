package com.kdj.food.controller;

import com.kdj.food.common.web.ApiPagination;
import com.kdj.food.common.web.PageResponse;
import com.kdj.food.dto.DishMaterial;
import com.kdj.food.dto.DishMaterialInput;
import com.kdj.food.dto.DishSummary;
import com.kdj.food.service.FoodService;
import com.kdj.food.service.FoodService.FoodSort;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(FoodController.DISHES_PATH)
public class FoodController {
    public static final String DISHES_PATH = "/api/v1/dishes";

    private final FoodService foods;

    public FoodController(FoodService foods) {
        this.foods = foods;
    }

    @GetMapping
    public PageResponse<DishSummary> search(
            @RequestParam(name = "q", defaultValue = "") String query,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var results = foods.search(query, parseSort(sort), ApiPagination.page(page, size));

        return PageResponse.from(results.map(DishSummary::from));
    }

    @GetMapping("/{dishId}")
    public DishDetail get(@PathVariable Long dishId) {
        FoodService.Detail detail = foods.get(dishId);

        return new DishDetail(
                DishSummary.from(detail.summary()),
                detail.description(),
                detail.authorId(),
                detail.authorName(),
                detail.materials()
        );
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreatedDish> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody DishRequest request
    ) {
        Long dishId = foods.create(
                Long.valueOf(jwt.getSubject()),
                request.name(),
                request.description(),
                request.imageId(),
                request.materials()
        );

        URI location = URI.create(DISHES_PATH + "/" + dishId);

        return ResponseEntity.created(location).body(new CreatedDish(dishId));
    }

    @PutMapping(path = "/{dishId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long dishId,
            @RequestBody DishRequest request
    ) {
        foods.update(
                dishId,
                Long.valueOf(jwt.getSubject()),
                request.name(),
                request.description(),
                request.imageId(),
                request.materials()
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{dishId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long dishId
    ) {
        foods.delete(dishId, Long.valueOf(jwt.getSubject()));

        return ResponseEntity.noContent().build();
    }

    private static FoodSort parseSort(String sort) {
        try {
            return FoodSort.valueOf(sort.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("지원하지 않는 요리 정렬 기준입니다.");
        }
    }

    record DishRequest(
            String name,
            String description,
            String imageId,
            List<DishMaterialInput> materials
    ) {
    }

    public record CreatedDish(Long dishId) {
    }

    public record DishDetail(
            DishSummary summary,
            String description,
            Long authorId,
            String authorName,
            List<DishMaterial> materials
    ) {
    }
}
