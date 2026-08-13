package com.kdj.commerce.web.dto.walk;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WalkCourseForm {
    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 30, message = "제목은 30자 이하만 입력 가능합니다.")
    private String name;

    @Size(max = 500, message = "후기는 500자 이하만 입력 가능합니다.")
    private String review;

    private Double startLat;
    private Double startLng;

    private Double endLat;
    private Double endLng;

    private String routeData;

    private Integer distance;
    private Integer duration;
}
