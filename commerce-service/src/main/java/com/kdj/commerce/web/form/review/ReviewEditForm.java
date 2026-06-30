package com.kdj.commerce.web.form.review;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewEditForm {
    private Long id;

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;
}
