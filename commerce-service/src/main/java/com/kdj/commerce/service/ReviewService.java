package com.kdj.commerce.service;

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

    public Page<Review> findByItemId(Long itemId, Pageable pageable) {
        return reviewRepository.findByItemId(itemId, pageable);
    }

    public List<Review> findByMemberId(Long memberId) {
        return reviewRepository.findByMemberId(memberId);
    }

    @Transactional
    public Long save(Review review) {
        Review savedReview = reviewRepository.save(review);

        return savedReview.getId();
    }

    @Transactional
    public void update(Long id, Review updateParam) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다 id=" + id));

        review.setTitle(updateParam.getTitle());
        review.setContent(updateParam.getContent());
    }
}
