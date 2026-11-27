package com.kdj.food.controller;

import com.kdj.food.service.FoodReviewService;
import com.kdj.food.service.FoodReviewService.ReviewView;
import com.kdj.food.common.web.ApiPagination;
import com.kdj.food.common.web.PageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping(FoodController.DISHES_PATH + "/{dishId}/reviews")
public class FoodReviewController {
    private final FoodReviewService reviews;

    public FoodReviewController(FoodReviewService reviews) {
        this.reviews = reviews;
    }

    @GetMapping
    public PageResponse<ReviewView> list(
            @PathVariable Long dishId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var results = reviews.findByFood(dishId, ApiPagination.latestFirst(page, size));

        return PageResponse.from(results);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedReview create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long dishId,
            @RequestBody JsonNode body
    ) {
        Scores scores = parseScores(body);

        if (scores.effort() == null || scores.taste() == null || scores.value() == null) {
            throw new IllegalArgumentException("등록에는 세 가지 평가 점수가 모두 필요합니다.");
        }

        Long reviewId = reviews.create(
                dishId,
                Long.valueOf(jwt.getSubject()),
                scores.effort(),
                scores.taste(),
                scores.value()
        );

        return new CreatedReview(reviewId);
    }

    @PatchMapping(path = "/{reviewId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long dishId,
            @PathVariable Long reviewId,
            @RequestBody JsonNode body
    ) {
        Scores scores = parseScores(body);

        reviews.update(
                dishId,
                reviewId,
                Long.valueOf(jwt.getSubject()),
                scores.effort(),
                scores.taste(),
                scores.value()
        );
    }

    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long dishId,
            @PathVariable Long reviewId
    ) {
        reviews.delete(dishId, reviewId, Long.valueOf(jwt.getSubject()));
    }

    private static Scores parseScores(JsonNode body) {
        if (!body.isObject()) {
            throw new IllegalArgumentException("평가 요청은 JSON 객체여야 합니다.");
        }

        return new Scores(score(body, "effort"), score(body, "taste"), score(body, "value"));
    }

    private static Integer score(JsonNode body, String field) {
        JsonNode value = body.get(field);
        if (value == null) {
            return null;
        }
        if (!value.isIntegralNumber() || !value.canConvertToInt()) {
            throw new IllegalArgumentException("평가 점수는 정수여야 합니다.");
        }
        return value.intValue();
    }

    private record Scores(Integer effort, Integer taste, Integer value) {
    }

    public record CreatedReview(Long reviewId) {
    }

}
