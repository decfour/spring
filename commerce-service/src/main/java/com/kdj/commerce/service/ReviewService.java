package com.kdj.commerce.service;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.notice.Notice;
import com.kdj.commerce.domain.review.Review;
import com.kdj.commerce.domain.review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public Review findOne(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다 ID=" + id));
    }

    public Page<Review> findReviewsByItemId(Long itemId, Pageable pageable) {
        return reviewRepository.findByItemId(itemId, pageable);
    }

    public List<Review> findItemsByMemberId(Long id) {
        return reviewRepository.findByMemberId(id);
    }


    @Transactional
    public Long saveReview(Review review) {
        Review savedReview = reviewRepository.save(review);

        return savedReview.getId();
    }
}
