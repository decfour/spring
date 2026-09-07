package com.kdj.commerce.domain.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemReviewRepository extends JpaRepository<ItemReview, Long> {
    Page<ItemReview> findByItemId(Pageable pageable, Long id);
    Page<ItemReview> findByCreatorId(Pageable pageable, Long id);
}
