package com.kdj.commerce.domain.review;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "item_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private Member creator;

    private LocalDateTime createdAt = LocalDateTime.now();

    public static ItemReview create(String title, String content, Item item, Member creator) {
        ItemReview review = new ItemReview();
        review.title = title;
        review.content = content;
        review.item = item;
        review.creator = creator;

        return review;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
