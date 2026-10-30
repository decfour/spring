package com.kdj.commerce.web.dto.walk;

import com.kdj.commerce.domain.walk.WalkCoursePin;
import java.time.LocalDateTime;

public record WalkCoursePinResponse(Long id, String title, String content, double latitude, double longitude,
                                    String creatorName, LocalDateTime createdAt, boolean deletable) {
    public static WalkCoursePinResponse from(WalkCoursePin pin, boolean deletable) {
        return new WalkCoursePinResponse(pin.getId(), pin.getTitle(), pin.getContent(), pin.getLatitude(),
                pin.getLongitude(), pin.getCreator().getName(), pin.getCreatedAt(), deletable);
    }
}
