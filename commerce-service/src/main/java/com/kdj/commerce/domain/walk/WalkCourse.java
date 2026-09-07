package com.kdj.commerce.domain.walk;

import com.kdj.commerce.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalkCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private Member creator;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Double startLat;
    private Double startLng;

    private Double endLat;
    private Double endLng;

    @Column(columnDefinition = "TEXT")
    private String routeData;

    private Integer distance;
    private Integer duration;

    private int likeCount;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(
            mappedBy = "walkCourse",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<WalkCourseTag> tags = new ArrayList<>();

    public static WalkCourse create(
            Member creator,
            String title,
            String content,
            Double startLat,
            Double startLng,
            Double endLat,
            Double endLng,
            String routeData,
            Integer distance,
            Integer duration
    ) {
        WalkCourse course = new WalkCourse();
        course.creator = creator;
        course.title = title;
        course.content = content;
        course.startLat = startLat;
        course.startLng = startLng;
        course.endLat = endLat;
        course.endLng = endLng;
        course.routeData = routeData;
        course.distance = distance;
        course.duration = duration;
        course.likeCount = 0;

        return course;
    }

    public void update (String title, String content) {
        this.title = title;
        this.content = content;
    }
}
