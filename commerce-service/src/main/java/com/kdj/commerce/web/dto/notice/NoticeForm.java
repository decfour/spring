package com.kdj.commerce.web.dto.notice;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoticeForm {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;
}
