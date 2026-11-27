package com.kdj.food.service;

import com.kdj.food.entity.Food;
import com.kdj.food.entity.FoodReview;
import com.kdj.food.repository.FoodRepository;
import com.kdj.food.repository.FoodReviewRepository;
import com.kdj.food.entity.Member;
import com.kdj.food.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FoodReviewService {
    private final FoodReviewRepository reviews;
    private final FoodRepository foods;
    private final MemberRepository members;

    public FoodReviewService(
            FoodReviewRepository reviews,
            FoodRepository foods,
            MemberRepository members
    ) {
        this.reviews = reviews;
        this.foods = foods;
        this.members = members;
    }

    public Page<ReviewView> findByFood(Long foodId, Pageable page) {
        if (!foods.existsById(foodId)) {
            throw new EntityNotFoundException("요리를 찾을 수 없습니다.");
        }
        return reviews.findAllByFood_Id(foodId, page).map(ReviewView::from);
    }

    public Page<ReviewView> findByMember(Long memberId, Pageable page) {
        return reviews.findAllByMember_Id(memberId, page).map(ReviewView::from);
    }

    @Transactional
    public Long create(
            Long foodId,
            Long memberId,
            int effort,
            int taste,
            int value
    ) {
        validateScores(effort, taste, value);

        Food food = foods.findById(foodId)
                .orElseThrow(() -> new EntityNotFoundException("요리를 찾을 수 없습니다."));

        if (food.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("자신이 등록한 요리는 평가할 수 없습니다.");
        }
        if (reviews.existsByFood_IdAndMember_Id(foodId, memberId)) {
            throw new DuplicateReviewException();
        }

        Member member = members.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        FoodReview review = new FoodReview(food, member, (byte) effort, (byte) taste, (byte) value);

        try {
            return reviews.saveAndFlush(review).getId();
        } catch (DataIntegrityViolationException exception) {
            if (exception.getCause() instanceof ConstraintViolationException violation
                    && violation.getConstraintName() != null
                    && violation.getConstraintName().endsWith("uk_food_review_food_member")) {
                throw new DuplicateReviewException();
            }
            throw exception;
        }
    }

    @Transactional
    public void update(
            Long foodId,
            Long reviewId,
            Long memberId,
            Integer effort,
            Integer taste,
            Integer value
    ) {
        if (effort == null && taste == null && value == null) {
            throw new IllegalArgumentException("수정할 평가 점수를 하나 이상 입력해주세요.");
        }

        FoodReview review = ownedReview(foodId, reviewId, memberId);
        int nextEffort = effort == null ? review.getEffort() : effort;
        int nextTaste = taste == null ? review.getTaste() : taste;
        int nextValue = value == null ? review.getValue() : value;

        validateScores(nextEffort, nextTaste, nextValue);
        review.updateScores((byte) nextEffort, (byte) nextTaste, (byte) nextValue);
    }

    @Transactional
    public void delete(Long foodId, Long reviewId, Long memberId) {
        reviews.delete(ownedReview(foodId, reviewId, memberId));
    }

    private FoodReview ownedReview(Long foodId, Long reviewId, Long memberId) {
        FoodReview review = reviews.findByIdAndFood_Id(reviewId, foodId)
                .orElseThrow(() -> new EntityNotFoundException("평가를 찾을 수 없습니다."));
        if (!review.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("작성자만 평가를 수정하거나 삭제할 수 있습니다.");
        }
        return review;
    }

    private static void validateScores(int effort, int taste, int value) {
        if (effort < 1 || effort > 5 || taste < 1 || taste > 5 || value < 1 || value > 5) {
            throw new IllegalArgumentException("모든 평가 점수는 1~5 사이여야 합니다.");
        }
    }

    public record ReviewView(
            Long id,
            Long foodId,
            Long authorId,
            int effort,
            int taste,
            int value,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        private static ReviewView from(FoodReview review) {
            return new ReviewView(
                    review.getId(),
                    review.getFood().getId(),
                    review.getMember().getId(),
                    review.getEffort(),
                    review.getTaste(),
                    review.getValue(),
                    review.getCreatedAt(),
                    review.getUpdatedAt()
            );
        }
    }
}
