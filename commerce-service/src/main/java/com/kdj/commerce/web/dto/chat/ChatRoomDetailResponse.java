package com.kdj.commerce.web.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChatRoomDetailResponse {
    private ChatCourseResponse course;
    private ChatRoomResponse room;
    private List<ChatMemberResponse> participants;
}

