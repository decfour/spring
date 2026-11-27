package com.kdj.food.repository;

import com.kdj.food.entity.Food;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FoodRepository extends JpaRepository<Food, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select food from Food food where food.id = :id")
    Optional<Food> findForUpdate(Long id);

    String SELECT_SCORES = """
            select food.id as id, food.name as name, food.imageKey as imageKey,
                   avg(review.effort) as effort, avg(review.taste) as taste, avg(review.value) as value,
                   (avg(review.effort) + avg(review.taste) + avg(review.value)) / 3.0 as efficiency,
                   count(review.id) as reviewCount
            from Food food left join FoodReview review on review.food = food
            """;
    String GROUP_FOOD = " group by food.id, food.name, food.imageKey, food.createdAt";
    String MATCH_NAME = " where locate(lower(:name), lower(food.name)) > 0";

    @Query(value = SELECT_SCORES + MATCH_NAME + GROUP_FOOD,
           countQuery = "select count(food) from Food food" + MATCH_NAME)
    Page<FoodScores> search(@Param("name") String name, Pageable pageable);

    @Query(value = SELECT_SCORES + " where food.member.id = :memberId" + GROUP_FOOD,
           countQuery = "select count(food) from Food food where food.member.id = :memberId")
    Page<FoodScores> findSummariesByMember(@Param("memberId") Long memberId, Pageable pageable);

    @Query(SELECT_SCORES + " where food.id = :foodId" + GROUP_FOOD)
    Optional<FoodScores> findSummary(@Param("foodId") Long foodId);
}
