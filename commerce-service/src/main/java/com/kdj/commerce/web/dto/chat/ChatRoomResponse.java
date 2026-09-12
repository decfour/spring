package com.kdj.commerce.web.dto.chat;

import com.kdj.commerce.domain.chat.ChatRoom;
import com.kdj.commerce.domain.chat.ChatRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatRoomResponse {
    private Long id;
    private String title;
    private String hostName;
    private Long hostId;
    private boolean open;
    private long count;
    private boolean joined;

    public static ChatRoomResponse from(ChatRoom chatRoom, long count, boolean joined) {
        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getTitle(),
                chatRoom.getHost().getName(),
                chatRoom.getHost().getId(),
                chatRoom.getStatus() == ChatRoomStatus.OPEN,
                count,
                joined
        );
    }
}

