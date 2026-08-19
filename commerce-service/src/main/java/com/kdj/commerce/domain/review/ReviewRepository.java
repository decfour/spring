package com.kdj.commerce.domain.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByItemId(Pageable pageable, Long id);
    Page<Review> findByMemberId(Pageable pageable, Long id);
}
