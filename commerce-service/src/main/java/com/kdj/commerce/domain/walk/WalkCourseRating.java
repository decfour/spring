package com.kdj.commerce.domain.walk;

import com.kdj.commerce.domain.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@org.hibernate.annotations.Check(constraints = "score between 1 and 5")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_course_rating_member", columnNames = {"course_id", "member_id"}))
public class WalkCourseRating {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private WalkCourse course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private int score;

    public static WalkCourseRating create(WalkCourse course, Member member, int score) {
        var rating = new WalkCourseRating();
        rating.course = course;
        rating.member = member;
        rating.update(score);

        return rating;
    }

    public void update(int score) {
        if (score < 1 || score > 5) throw new IllegalArgumentException("별점은 1~5점으로 선택해주세요.");
        this.score = score;
    }
}
