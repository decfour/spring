package com.kdj.commerce.service;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemRepository;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.review.ItemReview;
import com.kdj.commerce.domain.review.ItemReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemReviewService {
    private final ItemReviewRepository itemReviewRepository;
    private final ItemRepository itemRepository;

    public ItemReview findById(Long id) {
        return itemReviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다 id=" + id));
    }

    public Page<ItemReview> findByItemId(Long itemId, Pageable pageable) {
        return itemReviewRepository.findByItemId(pageable, itemId);
    }

    public Page<ItemReview> findByCreatorId(Pageable pageable, Long memberId) {
        return itemReviewRepository.findByCreatorId(pageable, memberId);
    }

    @Transactional
    public Long save(Long itemId, Member member, String title, String content) {
        Item item = itemRepository.findByIdWithLock(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다 id=" + itemId));
        ItemReview review = ItemReview.create(title, content, item, member);

        itemReviewRepository.save(review);

        return review.getId();
    }

    @Transactional
    public void update(Long id, String title, String content) {
        ItemReview review = itemReviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다 id=" + id));

        review.update(title, content);
    }

    @Transactional
    public void delete(Long id) {
        ItemReview review = itemReviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다 id=" + id));

        itemReviewRepository.delete(review);
    }
}
