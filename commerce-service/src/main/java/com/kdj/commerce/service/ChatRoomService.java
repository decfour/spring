package com.kdj.commerce.service;

import com.kdj.commerce.domain.chat.*;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import com.kdj.commerce.domain.walk.WalkCourse;
import com.kdj.commerce.domain.walk.WalkCourseRepository;
import com.kdj.commerce.web.dto.chat.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final WalkCourseRepository walkCourseRepository;
    private final MemberRepository memberRepository;

    public ChatCourseResponse findCourse(Long courseId) {
        return ChatCourseResponse.from(findWalkCourse(courseId));
    }

    public Page<ChatRoomResponse> findRooms(
            Long courseId,
            Long memberId,
            boolean mine,
            Pageable pageable
    ) {
        Page<ChatRoom> chatRooms = mine
                ? chatRoomRepository.findJoinedRooms(courseId, memberId, pageable)
                : chatRoomRepository.findByWalkCourseIdAndStatusOrderByCreatedAtDescIdDesc(
                        courseId, ChatRoomStatus.OPEN, pageable
                );

        return chatRooms.map(chatRoom -> {
            long count = chatMemberRepository.countByChatRoomId(chatRoom.getId());
            boolean joined = chatMemberRepository.existsByChatRoomIdAndMemberId(chatRoom.getId(), memberId);

            return ChatRoomResponse.from(chatRoom, count, joined);
        });
    }

    public ChatRoomDetailResponse findDetail(Long courseId, Long roomId, Long memberId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        validateCourse(chatRoom, courseId);
        validateOpen(chatRoom);
        validateParticipant(roomId, memberId);

        List<ChatMemberResponse> participants = chatMemberRepository
                .findByChatRoomIdOrderByJoinedAtAscIdAsc(roomId)
                .stream()
                .map(ChatMemberResponse::from)
                .toList();

        return new ChatRoomDetailResponse(
                ChatCourseResponse.from(chatRoom.getWalkCourse()),
                ChatRoomResponse.from(chatRoom, participants.size(), true),
                participants
        );
    }

    @Transactional
    public Long save(Long courseId, Long memberId, String title) {
        Member host = findMember(memberId);
        WalkCourse walkCourse = findWalkCourse(courseId);
        ChatRoom chatRoom = ChatRoom.create(walkCourse, host, title);

        chatRoomRepository.save(chatRoom);
        chatMemberRepository.save(ChatMember.create(chatRoom, host));

        return chatRoom.getId();
    }

    // 잠금을 기다린 뒤 최신 참여 인원을 조회해야 하므로 READ_COMMITTED를 사용
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void join(Long courseId, Long roomId, Long memberId) {
        Member member = findMember(memberId);
        ChatRoom chatRoom = findChatRoomWithLock(roomId);

        validateCourse(chatRoom, courseId);
        validateOpen(chatRoom);

        if (chatMemberRepository.existsByChatRoomIdAndMemberId(roomId, memberId)) {
            throw new IllegalStateException("이미 참여 중인 채팅방입니다.");
        }

        if (chatMemberRepository.countByChatRoomId(roomId) >= ChatRoom.MAX_PARTICIPANTS) {
            throw new IllegalStateException("채팅방은 최대 5명까지 참여할 수 있습니다.");
        }

        chatMemberRepository.save(ChatMember.create(chatRoom, member));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void leave(Long courseId, Long roomId, Long memberId) {
        ChatRoom chatRoom = findChatRoomWithLock(roomId);

        validateCourse(chatRoom, courseId);
        validateOpen(chatRoom);
        validateParticipant(roomId, memberId);

        if (chatRoom.getHost().getId().equals(memberId)) {
            chatRoom.close();
            chatMemberRepository.deleteByChatRoomId(roomId);
        } else {
            chatMemberRepository.deleteByChatRoomIdAndMemberId(roomId, memberId);
        }
    }

    private WalkCourse findWalkCourse(Long courseId) {
        return walkCourseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 산책 코스입니다."));
    }

    private Member findMember(Long memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    private ChatRoom findChatRoomWithLock(Long roomId) {
        return chatRoomRepository.findByIdWithLock(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));
    }

    private void validateCourse(ChatRoom chatRoom, Long courseId) {
        if (!chatRoom.getWalkCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("해당 코스의 채팅방이 아닙니다.");
        }
    }

    private void validateOpen(ChatRoom chatRoom) {
        if (chatRoom.getStatus() != ChatRoomStatus.OPEN) {
            throw new IllegalStateException("종료된 채팅방입니다.");
        }
    }

    private void validateParticipant(Long roomId, Long memberId) {
        if (memberId == null || !chatMemberRepository.existsByChatRoomIdAndMemberId(roomId, memberId)) {
            throw new IllegalStateException("채팅방 참여자만 접근할 수 있습니다.");
        }
    }
}
