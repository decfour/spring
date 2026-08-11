package com.kdj.commerce.web.dto.walk;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WalkCourseForm {
    private String name;
    private String review;

    private Double startLat;
    private Double startLng;

    private Double endLat;
    private Double endLng;

    private String routeData;

    private Integer distance;
    private Integer duration;
}
