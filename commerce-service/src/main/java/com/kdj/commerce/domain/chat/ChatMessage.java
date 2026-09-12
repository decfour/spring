package com.kdj.commerce.domain.chat;

import com.kdj.commerce.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "chat_message", indexes = {
        @Index(name = "idx_chat_message_room_id", columnList = "chat_room_id, id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false, updatable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false, updatable = false)
    private Member sender;

    @Column(nullable = false, length = 1000, updatable = false)
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public static ChatMessage create(ChatRoom chatRoom, Member sender, String content) {
        if (content == null || content.isBlank() || content.length() > 1000) {
            throw new IllegalArgumentException("메시지는 1~1000자여야 합니다.");
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.chatRoom = Objects.requireNonNull(chatRoom, "채팅방은 필수입니다.");
        chatMessage.sender = Objects.requireNonNull(sender, "발신자는 필수입니다.");
        chatMessage.content = content;

        return chatMessage;
    }
}
