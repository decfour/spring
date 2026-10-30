package com.kdj.commerce.domain.review;

import com.kdj.commerce.domain.walk.WalkCourse;
import com.kdj.commerce.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "walk_course_review", indexes = @Index(name = "idx_course_review_course", columnList = "course_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalkCourseReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private WalkCourse course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private Member creator;

    private LocalDateTime createdAt = LocalDateTime.now();

    public static WalkCourseReview create(String title, String content, WalkCourse course, Member creator) {
        WalkCourseReview review = new WalkCourseReview();
        review.title = title;
        review.content = content;
        review.course = course;
        review.creator = creator;

        return review;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}

