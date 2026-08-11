package com.kdj.commerce.domain.walk;

import com.kdj.commerce.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalkCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String review;

    private Double startLat;
    private Double startLng;

    private Double endLat;
    private Double endLng;

    @Column(columnDefinition = "TEXT")
    private String routeData;

    private Integer distance;

    private Integer duration;

    private LocalDateTime createdAt = LocalDateTime.now();

    public static WalkCourse create(
            Member member,
            String name,
            String review,
            Double startLat,
            Double startLng,
            Double endLat,
            Double endLng,
            String routeData,
            Integer distance,
            Integer duration
    ) {
        WalkCourse course = new WalkCourse();

        course.member = member;
        course.name = name;
        course.review = review;
        course.startLat = startLat;
        course.startLng = startLng;
        course.endLat = endLat;
        course.endLng = endLng;
        course.routeData = routeData;
        course.distance = distance;
        course.duration = duration;

        return course;
    }
}
