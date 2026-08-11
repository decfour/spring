package com.kdj.commerce.web.dto.walk;

import com.kdj.commerce.domain.walk.WalkCourse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WalkCourseResponse {
    private Long id;
    private String name;
    private String review;

    private Double startLat;
    private Double startLng;

    private Double endLat;
    private Double endLng;

    private String routeData;

    private Integer distance;
    private Integer duration;
    private Integer likeCount;

    public static WalkCourseResponse from(WalkCourse course) {
        return new WalkCourseResponse(
                course.getId(),
                course.getName(),
                course.getReview(),
                course.getStartLat(),
                course.getStartLng(),
                course.getEndLat(),
                course.getEndLng(),
                course.getRouteData(),
                course.getDistance(),
                course.getDuration(),
                course.getLikeCount()
        );
    }
}
