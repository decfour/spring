package com.kdj.commerce.web.dto.chat;

import com.kdj.commerce.domain.walk.WalkCourse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatCourseResponse {
    private Long id;
    private String title;
    private Integer distance;
    private Integer duration;

    public static ChatCourseResponse from(WalkCourse course) {
        return new ChatCourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDistance(),
                course.getDuration()
        );
    }
}

