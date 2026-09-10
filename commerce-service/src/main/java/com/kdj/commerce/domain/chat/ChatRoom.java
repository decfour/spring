package com.kdj.commerce.domain.chat;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.walk.WalkCourse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {
    public static final int MAX_PARTICIPANTS = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "walk_course_id", nullable = false)
    private WalkCourse walkCourse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id", nullable = false)
    private Member host;

    @Column(nullable = false, length = 30)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatRoomStatus status = ChatRoomStatus.OPEN;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public static ChatRoom create(WalkCourse walkCourse, Member host, String title) {
        if (title == null || title.isBlank() || title.length() > 30) {
            throw new IllegalArgumentException("제목은 1~30자여야 합니다.");
        }

        ChatRoom room = new ChatRoom();
        room.walkCourse = Objects.requireNonNull(walkCourse, "산책 코스는 필수입니다.");
        room.host = Objects.requireNonNull(host, "방장은 필수입니다.");
        room.title = title;

        return room;
    }

    public void close() {
        this.status = ChatRoomStatus.CLOSE;
    }
}
