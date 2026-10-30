package com.kdj.commerce.web.dto.walk;

import jakarta.validation.constraints.*;

public record WalkCoursePinRequest(
        @NotBlank @Size(max = 80) String title,
        @NotBlank @Size(max = 1000) String content,
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude) {
}
