package com.kdj.commerce.web.dto.chat;

import com.kdj.commerce.domain.chat.ChatMember;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMemberResponse {
    private Long id;
    private String name;

    public static ChatMemberResponse from(ChatMember chatMember) {
        return new ChatMemberResponse(
                chatMember.getMember().getId(),
                chatMember.getMember().getName()
        );
    }
}

