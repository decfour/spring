package com.kdj.commerce.domain.community;

import com.kdj.commerce.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false)
    private String title;

    @Lob
    @Column(length = 500, nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member writer;

    private int viewCount;
    private int likeCount;

    private LocalDateTime createdDate = LocalDateTime.now();;

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public static Post create(String title, String content, Member member) {
        Post post = new Post();
        post.title = title;
        post.content = content;
        post.writer = member;
        post.viewCount = 0;
        post.likeCount = 0;

        return post;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
