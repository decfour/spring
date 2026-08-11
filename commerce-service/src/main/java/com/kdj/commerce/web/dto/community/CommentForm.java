package com.kdj.commerce.web.dto.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentForm {
    @NotBlank(message = "댓글을 입력해주세요.")
    @Size(max = 500, message = "댓글은 500자 이하만 입력 가능합니다.")
    private String content;
}
