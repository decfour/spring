package com.kdj.commerce.service;

import com.kdj.commerce.domain.chat.ChatMessage;
import com.kdj.commerce.domain.chat.ChatMessageRepository;
import com.kdj.commerce.domain.chat.ChatRoom;
import com.kdj.commerce.domain.chat.ChatRoomRepository;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.web.dto.chat.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberService memberService;

    public ChatMessageResponse send(
            Long roomId,
            String email,
            String content
    ) {
        Member sender = memberService.findByEmail(email);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        ChatMessage message = ChatMessage.create(
                chatRoom,
                sender,
                content
        );

        ChatMessage savedMessage = chatMessageRepository.save(message);

        return new ChatMessageResponse(
                savedMessage.getId(),
                savedMessage.getSender().getName(),
                savedMessage.getContent(),
                savedMessage.getCreatedAt()
        );
    }
}
