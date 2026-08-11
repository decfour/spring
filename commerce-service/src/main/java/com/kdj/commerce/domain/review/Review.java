package com.kdj.commerce.domain.review;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {
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
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime createdDate = LocalDateTime.now();

    public static Review create(String title, String content, Item item, Member member) {
        Review review = new Review();
        review.title = title;
        review.content = content;
        review.item = item;
        review.member = member;

        return review;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
