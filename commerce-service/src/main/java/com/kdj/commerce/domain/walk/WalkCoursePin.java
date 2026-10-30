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
@Table(indexes = @Index(name = "idx_walk_pin_course", columnList = "course_id"))
public class WalkCoursePin {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)

    @JoinColumn(name = "course_id", nullable = false)
    private WalkCourse course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)

    @JoinColumn(name = "creator_id", nullable = false)
    private Member creator;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, length = 1000)
    private String content;

    private double latitude;
    private double longitude;

    private LocalDateTime createdAt = LocalDateTime.now();

    public static WalkCoursePin create(WalkCourse course, Member creator, String title, String content,
                                       double latitude, double longitude) {
        var pin = new WalkCoursePin();
        pin.course = course;
        pin.creator = creator;
        pin.title = title;
        pin.content = content;
        pin.latitude = latitude;
        pin.longitude = longitude;
        return pin;
    }
}
