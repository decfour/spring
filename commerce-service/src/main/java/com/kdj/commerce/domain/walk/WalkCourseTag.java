package com.kdj.commerce.domain.walk;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalkCourseTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private WalkCourse walkCourse;

    @Enumerated(EnumType.STRING)
    private WalkTag tag;

    public static WalkCourseTag create(WalkCourse walkCourse, WalkTag tag) {
        WalkCourseTag walkCourseTag = new WalkCourseTag();
        walkCourseTag.walkCourse = walkCourse;
        walkCourseTag.tag = tag;

        return walkCourseTag;
    }
}
