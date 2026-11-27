package com.kdj.food.controller;

import com.kdj.food.dto.DishSummary;
import com.kdj.food.service.FoodReviewService;
import com.kdj.food.service.FoodReviewService.ReviewView;
import com.kdj.food.service.FoodService;
import com.kdj.food.service.MemberService;
import com.kdj.food.common.web.ApiPagination;
import com.kdj.food.common.web.PageResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/me")
public class MemberController {
    private final MemberService members;
    private final FoodService foods;
    private final FoodReviewService reviews;

    public MemberController(MemberService members, FoodService foods, FoodReviewService reviews) {
        this.members = members;
        this.foods = foods;
        this.reviews = reviews;
    }

    @GetMapping
    public MemberService.Profile getProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return members.getProfile(Long.valueOf(jwt.getSubject()));
    }

    @PatchMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public MemberService.Profile rename(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody RenameRequest request
    ) {
        return members.rename(Long.valueOf(jwt.getSubject()), request.name());
    }

    @GetMapping("/dishes")
    public PageResponse<DishSummary> myDishes(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());
        var results = foods.findByMember(memberId, ApiPagination.latestFirst(page, size));

        return PageResponse.from(results.map(DishSummary::from));
    }

    @GetMapping("/reviews")
    public PageResponse<ReviewView> myReviews(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long memberId = Long.valueOf(jwt.getSubject());
        var results = reviews.findByMember(memberId, ApiPagination.latestFirst(page, size));

        return PageResponse.from(results);
    }

    record RenameRequest(String name) {
    }
}
