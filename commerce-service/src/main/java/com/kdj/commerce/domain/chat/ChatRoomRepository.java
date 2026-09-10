package com.kdj.commerce.domain.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 참여와 퇴장은 같은 방의 잠금을 획득한 후 처리한다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ChatRoom r where r.id = :id")
    Optional<ChatRoom> findByIdForUpdate(@Param("id") Long id);

    Page<ChatRoom> findByWalkCourseIdOrderByCreatedAtDescIdDesc(
            Long walkCourseId, Pageable pageable
    );

    Page<ChatRoom> findByWalkCourseIdAndStatusOrderByCreatedAtDescIdDesc(
            Long walkCourseId, ChatRoomStatus status, Pageable pageable
    );

    @Query("""
            select r from ChatRoom r
            where r.walkCourse.id = :walkCourseId
              and r.status = com.kdj.commerce.domain.chat.ChatRoomStatus.OPEN
              and exists (
                  select m.id from ChatMember m
                  where m.chatRoom = r and m.member.id = :memberId
              )
            order by r.createdAt desc, r.id desc
            """)
    Page<ChatRoom> findJoinedRooms(
            @Param("walkCourseId") Long walkCourseId,
            @Param("memberId") Long memberId, Pageable pageable
    );
}
