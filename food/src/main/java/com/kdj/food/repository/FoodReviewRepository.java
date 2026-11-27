package com.kdj.food.repository;

import com.kdj.food.entity.FoodReview;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface FoodReviewRepository extends JpaRepository<FoodReview, Long> {
    boolean existsByFood_IdAndMember_Id(Long foodId, Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<FoodReview> findByIdAndFood_Id(Long id, Long foodId);

    Page<FoodReview> findAllByFood_Id(Long foodId, Pageable pageable);

    Page<FoodReview> findAllByMember_Id(Long memberId, Pageable pageable);
}
