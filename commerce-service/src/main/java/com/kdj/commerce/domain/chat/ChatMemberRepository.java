package com.kdj.commerce.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
    boolean existsByChatRoomIdAndMemberId(Long chatRoomId, Long memberId);

    long countByChatRoomId(Long chatRoomId);

    List<ChatMember> findByChatRoomIdOrderByJoinedAtAscIdAsc(Long chatRoomId);

    void deleteByChatRoomIdAndMemberId(Long chatRoomId, Long memberId);

    void deleteByChatRoomId(Long chatRoomId);
}
