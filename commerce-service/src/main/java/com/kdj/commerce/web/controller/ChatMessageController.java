package com.kdj.commerce.web.controller;

import com.kdj.commerce.service.ChatMessageService;
import com.kdj.commerce.web.dto.chat.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/walk/course/{courseId}/chat/{roomId}/messages")
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    @GetMapping
    public Slice<ChatMessageResponse> getMessages(
            @PathVariable Long courseId,
            @PathVariable Long roomId,
            @RequestParam(required = false) Long before
    ) {
        if (before == null) {
            return chatMessageService.getRecentMessages(roomId);
        }

        return chatMessageService.getPreviousMessages(roomId, before);
    }
}
