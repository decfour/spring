package com.kdj.commerce.web.dto.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostForm {
    private Long id;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 30, message = "제목은 최대 30자 입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;
}
