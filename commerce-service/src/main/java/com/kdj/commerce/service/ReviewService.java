package com.kdj.commerce.service;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemRepository;
import com.kdj.commerce.domain.member.Member;
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
    private final ItemRepository itemRepository;

    public Review findOne(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다 id=" + id));
    }

    public Page<Review> findByItemId(Long itemId, Pageable pageable) {
        return reviewRepository.findByItemId(itemId, pageable);
    }

    public List<Review> findByMemberId(Long memberId) {
        return reviewRepository.findByMemberId(memberId);
    }

    @Transactional
    public Long save(Long itemId, Member member, String title, String content) {
        Item item = itemRepository.findByIdWithLock(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다 id=" + itemId));
        Review review = Review.create(title, content, item, member);

        reviewRepository.save(review);

        return review.getId();
    }

    @Transactional
    public void update(Long id, String title, String content) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다 id=" + id));

        review.update(title, content);
    }

    @Transactional
    public void delete(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다 id=" + id));

        reviewRepository.delete(review);
    }
}
