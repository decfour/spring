package com.kdj.commerce.domain.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByItemId(Long itemId, Pageable pageable);
    List<Review> findByMemberId(Long id);
}
