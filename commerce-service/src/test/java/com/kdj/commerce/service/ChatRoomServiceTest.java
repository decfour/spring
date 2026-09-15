package com.kdj.commerce.service;

import com.kdj.commerce.domain.chat.*;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import com.kdj.commerce.domain.walk.WalkCourse;
import com.kdj.commerce.domain.walk.WalkCourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {
    @Mock ChatRoomRepository roomRepository;
    @Mock ChatMemberRepository participantRepository;
    @Mock WalkCourseRepository courseRepository;
    @Mock MemberRepository memberRepository;
    @InjectMocks ChatRoomService service;

    private Member host;
    private Member guest;
    private WalkCourse course;
    private ChatRoom room;

    @BeforeEach
    void setUp() {
        host = mock(Member.class);
        guest = mock(Member.class);
        course = mock(WalkCourse.class);
        room = ChatRoom.create(course, host, "함께 걸어요");
    }

    @Test
    @DisplayName("채팅방 생성 시 방장을 참여자로 등록한다")
    void createRegistersHost() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(host));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(roomRepository.save(any(ChatRoom.class))).thenAnswer(call -> call.getArgument(0));

        service.save(10L, 1L, "함께 걸어요");

        ArgumentCaptor<ChatMember> saved = ArgumentCaptor.forClass(ChatMember.class);
        verify(participantRepository).save(saved.capture());
        assertThat(saved.getValue().getMember()).isSameAs(host);
        assertThat(saved.getValue().getChatRoom().getHost()).isSameAs(host);
        assertThat(saved.getValue().getChatRoom().getStatus()).isEqualTo(ChatRoomStatus.OPEN);
    }

    private void prepareGuestJoin() {
        when(course.getId()).thenReturn(100L);
        when(memberRepository.findById(2L)).thenReturn(Optional.of(guest));
        when(roomRepository.findByIdWithLock(10L)).thenReturn(Optional.of(room));
    }

    @Test
    @DisplayName("참여자가 4명이면 다섯 번째 회원이 참여할 수 있다")
    void fifthParticipantCanJoin() {
        prepareGuestJoin();
        when(participantRepository.countByChatRoomId(10L)).thenReturn(4L);
        service.join(100L, 10L, 2L);
        ArgumentCaptor<ChatMember> saved = ArgumentCaptor.forClass(ChatMember.class);
        verify(participantRepository).save(saved.capture());
        assertThat(saved.getValue().getMember()).isSameAs(guest);
        assertThat(saved.getValue().getChatRoom()).isSameAs(room);
        assertThat(room.getStatus()).isEqualTo(ChatRoomStatus.OPEN);
    }

    @Test
    @DisplayName("참여자가 5명이면 추가 참여를 거부한다")
    void sixthParticipantCannotJoin() {
        prepareGuestJoin();
        when(participantRepository.countByChatRoomId(10L)).thenReturn(5L);
        assertThatThrownBy(() -> service.join(100L, 10L, 2L))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("5명");
        verify(participantRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 참여한 회원의 중복 참여를 거부한다")
    void duplicateJoinIsRejected() {
        prepareGuestJoin();
        when(participantRepository.existsByChatRoomIdAndMemberId(10L, 2L)).thenReturn(true);
        assertThatThrownBy(() -> service.join(100L, 10L, 2L))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("이미 참여");
        verify(participantRepository, never()).save(any());
    }

    @Test
    @DisplayName("종료된 채팅방의 참여를 거부한다")
    void closedRoomRejectsJoin() {
        prepareGuestJoin();
        room.close();
        assertThatThrownBy(() -> service.join(100L, 10L, 2L))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("종료");
        verifyNoInteractions(participantRepository);
    }

    @Test
    @DisplayName("방장이 퇴장하면 채팅방을 종료하고 모든 참여자를 삭제한다")
    void hostLeavingClosesRoomAndRemovesEveryone() {
        when(course.getId()).thenReturn(100L);
        when(host.getId()).thenReturn(1L);
        when(roomRepository.findByIdWithLock(10L)).thenReturn(Optional.of(room));
        when(participantRepository.existsByChatRoomIdAndMemberId(10L, 1L)).thenReturn(true);
        service.leave(100L, 10L, 1L);
        assertThat(room.getStatus()).isEqualTo(ChatRoomStatus.CLOSE);
        verify(participantRepository).deleteByChatRoomId(10L);
        verify(participantRepository, never()).deleteByChatRoomIdAndMemberId(any(), any());
    }

    @Test
    @DisplayName("일반 참여자가 퇴장하면 해당 참여자만 삭제하고 채팅방을 유지한다")
    void guestLeavingKeepsRoomOpen() {
        when(course.getId()).thenReturn(100L);
        when(roomRepository.findByIdWithLock(10L)).thenReturn(Optional.of(room));
        when(host.getId()).thenReturn(1L);
        when(participantRepository.existsByChatRoomIdAndMemberId(10L, 2L)).thenReturn(true);
        service.leave(100L, 10L, 2L);
        assertThat(room.getStatus()).isEqualTo(ChatRoomStatus.OPEN);
        verify(participantRepository).deleteByChatRoomIdAndMemberId(10L, 2L);
        verify(participantRepository, never()).deleteByChatRoomId(any());
    }

    @Test
    @DisplayName("참여하지 않은 회원은 채팅방 상세와 참여자를 조회할 수 없다")
    void outsiderCannotReadParticipants() {
        when(course.getId()).thenReturn(100L);
        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
        assertThatThrownBy(() -> service.findDetail(100L, 10L, 2L))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("참여자만");
        verify(participantRepository, never()).findByChatRoomIdOrderByJoinedAtAscIdAsc(any());
    }

    @Test
    @DisplayName("참여하지 않은 회원의 퇴장 요청을 거부한다")
    void outsiderCannotLeave() {
        when(course.getId()).thenReturn(100L);
        when(roomRepository.findByIdWithLock(10L)).thenReturn(Optional.of(room));
        assertThatThrownBy(() -> service.leave(100L, 10L, 2L))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("참여자만");
        verify(participantRepository, never()).deleteByChatRoomId(any());
        verify(participantRepository, never()).deleteByChatRoomIdAndMemberId(any(), any());
    }
    @Test
    @DisplayName("다른 산책 코스의 채팅방에는 참여할 수 없다")
    void wrongCourseCannotJoin() {
        prepareGuestJoin();

        assertThatThrownBy(() -> service.join(999L, 10L, 2L))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("해당 코스");

        verifyNoInteractions(participantRepository);
    }

    @Test
    @DisplayName("다른 산책 코스로 퇴장을 요청하면 채팅방을 종료하지 않는다")
    void wrongCourseCannotCloseRoom() {
        when(course.getId()).thenReturn(100L);
        when(roomRepository.findByIdWithLock(10L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> service.leave(999L, 10L, 1L))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("해당 코스");

        assertThat(room.getStatus()).isEqualTo(ChatRoomStatus.OPEN);
        verifyNoInteractions(participantRepository);
    }

    @Test
    @DisplayName("열린 채팅방만 대상으로 페이지 조회를 수행한다")
    void listUsesOpenRoomsBeforePagination() {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 4);
        when(roomRepository.findByWalkCourseIdAndStatusOrderByCreatedAtDescIdDesc(
                100L, ChatRoomStatus.OPEN, pageable))
                .thenReturn(org.springframework.data.domain.Page.empty(pageable));

        assertThat(service.findRooms(100L, 1L, false, pageable).isEmpty()).isTrue();
        verify(roomRepository).findByWalkCourseIdAndStatusOrderByCreatedAtDescIdDesc(
                100L, ChatRoomStatus.OPEN, pageable);
    }

    @Test
    @DisplayName("채팅방 상세 응답에 코스와 참여자 정보를 포함한다")
    void detailMapsParticipantsInsideService() {
        when(course.getId()).thenReturn(100L);
        when(host.getId()).thenReturn(1L);
        when(host.getName()).thenReturn("방장");
        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(participantRepository.existsByChatRoomIdAndMemberId(10L, 1L)).thenReturn(true);
        when(participantRepository.findByChatRoomIdOrderByJoinedAtAscIdAsc(10L))
                .thenReturn(java.util.List.of(ChatMember.create(room, host)));

        var detail = service.findDetail(100L, 10L, 1L);

        assertThat(detail.getCourse().getId()).isEqualTo(100L);
        assertThat(detail.getRoom().getCount()).isEqualTo(1L);
        assertThat(detail.getRoom().isJoined()).isTrue();
        assertThat(detail.getParticipants()).extracting("name").containsExactly("방장");
        verify(participantRepository, never()).countByChatRoomId(any());
    }
}
