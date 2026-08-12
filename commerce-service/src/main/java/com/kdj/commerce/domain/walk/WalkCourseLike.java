package com.kdj.commerce.domain.walk;

import com.kdj.commerce.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_walk_course",
                        columnNames = {"member_id", "walk_course_id"})})
public class WalkCourseLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "walk_course_id")
    private WalkCourse walkCourse;

    public static WalkCourseLike create(Member member, WalkCourse walkCourse) {
        WalkCourseLike walkCourseLike = new WalkCourseLike();
        walkCourseLike.member = member;
        walkCourseLike.walkCourse = walkCourse;

        return walkCourseLike;
    }
}
