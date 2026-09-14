package com.kdj.commerce.web.dto.chat;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        String senderName,
        String content,
        LocalDateTime createdAt
) {
}