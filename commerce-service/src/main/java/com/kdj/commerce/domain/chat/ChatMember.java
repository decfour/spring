package com.kdj.commerce.domain.chat;

import com.kdj.commerce.domain.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "chat_member", uniqueConstraints = {
        @UniqueConstraint(name = "uk_chat_member", columnNames = {"chat_room_id", "member_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    public static ChatMember create(ChatRoom chatRoom, Member member) {
        ChatMember chatMember = new ChatMember();
        chatMember.chatRoom = Objects.requireNonNull(chatRoom, "채팅방은 필수입니다.");
        chatMember.member = Objects.requireNonNull(member, "참여자는 필수입니다.");

        return chatMember;
    }
}
