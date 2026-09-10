package com.kdj.commerce.service;

import com.kdj.commerce.domain.chat.*;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import com.kdj.commerce.domain.walk.WalkCourse;
import com.kdj.commerce.domain.walk.WalkCourseRepository;
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

    @Transactional
    public Long create(Long walkCourseId, Long memberId, String title) {
        Member host = findMember(memberId);
        WalkCourse course = walkCourseRepository.findById(walkCourseId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 산책 코스입니다."));

        ChatRoom room = chatRoomRepository.save(ChatRoom.create(course, host, title));

        chatMemberRepository.save(ChatMember.create(room, host));

        return room.getId();
    }

    // 잠금을 기다린 후에도 최신 참여 인원으로 판단하도록 READ_COMMITTED를 사용한다.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void join(Long chatRoomId, Long memberId) {
        Member member = findMember(memberId);
        ChatRoom room = findRoomForUpdate(chatRoomId);

        requireOpen(room);

        if (chatMemberRepository.existsByChatRoomIdAndMemberId(chatRoomId, memberId)) {
            throw new IllegalStateException("이미 참여 중인 채팅방입니다.");
        }
        if (chatMemberRepository.countByChatRoomId(chatRoomId) >= ChatRoom.MAX_PARTICIPANTS) {
            throw new IllegalStateException("채팅방은 최대 5명까지 참여할 수 있습니다.");
        }

        chatMemberRepository.save(ChatMember.create(room, member));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void leave(Long chatRoomId, Long memberId) {
        findMember(memberId);
        ChatRoom room = findRoomForUpdate(chatRoomId);

        requireOpen(room);
        requireParticipant(chatRoomId, memberId);

        if (room.getHost().getId().equals(memberId)) {
            room.close();
            chatMemberRepository.deleteByChatRoomId(chatRoomId);
        } else {
            chatMemberRepository.deleteByChatRoomIdAndMemberId(chatRoomId, memberId);
        }
    }

    public Page<ChatRoom> findByWalkCourseId(Long walkCourseId, Pageable pageable) {
        return chatRoomRepository
                .findByWalkCourseIdOrderByCreatedAtDescIdDesc(walkCourseId, pageable);
    }

    public Page<ChatRoom> findByWalkCourseIdAndStatus(
            Long walkCourseId, ChatRoomStatus status, Pageable pageable) {

        return chatRoomRepository
                .findByWalkCourseIdAndStatusOrderByCreatedAtDescIdDesc(
                walkCourseId, status, pageable
                );
    }

    public Page<ChatRoom> findJoinedRooms(Long walkCourseId, Long memberId, Pageable pageable) {
        findMember(memberId);

        return chatRoomRepository.findJoinedRooms(walkCourseId, memberId, pageable);
    }

    public ChatRoom findById(Long chatRoomId, Long memberId) {
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        requireOpen(room);
        requireParticipant(chatRoomId, memberId);

        return room;
    }

    public List<ChatMember> findParticipants(Long chatRoomId, Long memberId) {
        findById(chatRoomId, memberId);

        return chatMemberRepository.findByChatRoomIdOrderByJoinedAtAscIdAsc(chatRoomId);
    }

    private Member findMember(Long memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    private ChatRoom findRoomForUpdate(Long chatRoomId) {
        return chatRoomRepository.findByIdForUpdate(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));
    }

    private void requireOpen(ChatRoom room) {
        if (room.getStatus() != ChatRoomStatus.OPEN) {
            throw new IllegalStateException("종료된 채팅방입니다.");
        }
    }

    private void requireParticipant(Long chatRoomId, Long memberId) {
        if (memberId == null || !chatMemberRepository.existsByChatRoomIdAndMemberId(chatRoomId, memberId)) {
            throw new IllegalStateException("채팅방 참여자만 접근할 수 있습니다.");
        }
    }
}
