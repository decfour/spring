package com.kdj.commerce.service;

import com.kdj.commerce.domain.chat.*;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import com.kdj.commerce.domain.walk.WalkCourse;
import com.kdj.commerce.domain.walk.WalkCourseRepository;
import org.junit.jupiter.api.BeforeEach;
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
    void createRegistersHost() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(host));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(roomRepository.save(any(ChatRoom.class))).thenAnswer(call -> call.getArgument(0));

        service.create(10L, 1L, "함께 걸어요");

        ArgumentCaptor<ChatMember> saved = ArgumentCaptor.forClass(ChatMember.class);
        verify(participantRepository).save(saved.capture());
        assertThat(saved.getValue().getMember()).isSameAs(host);
        assertThat(saved.getValue().getChatRoom().getHost()).isSameAs(host);
        assertThat(saved.getValue().getChatRoom().getStatus()).isEqualTo(ChatRoomStatus.OPEN);
    }

    private void prepareGuestJoin() {
        when(memberRepository.findById(2L)).thenReturn(Optional.of(guest));
        when(roomRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(room));
    }

    @Test
    void fifthParticipantCanJoin() {
        prepareGuestJoin();
        when(participantRepository.countByChatRoomId(10L)).thenReturn(4L);
        service.join(10L, 2L);
        ArgumentCaptor<ChatMember> saved = ArgumentCaptor.forClass(ChatMember.class);
        verify(participantRepository).save(saved.capture());
        assertThat(saved.getValue().getMember()).isSameAs(guest);
        assertThat(saved.getValue().getChatRoom()).isSameAs(room);
        assertThat(room.getStatus()).isEqualTo(ChatRoomStatus.OPEN);
    }

    @Test
    void sixthParticipantCannotJoin() {
        prepareGuestJoin();
        when(participantRepository.countByChatRoomId(10L)).thenReturn(5L);
        assertThatThrownBy(() -> service.join(10L, 2L))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("5명");
        verify(participantRepository, never()).save(any());
    }

    @Test
    void duplicateJoinIsRejected() {
        prepareGuestJoin();
        when(participantRepository.existsByChatRoomIdAndMemberId(10L, 2L)).thenReturn(true);
        assertThatThrownBy(() -> service.join(10L, 2L))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("이미 참여");
        verify(participantRepository, never()).save(any());
    }

    @Test
    void closedRoomRejectsJoin() {
        prepareGuestJoin();
        room.close();
        assertThatThrownBy(() -> service.join(10L, 2L))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("종료");
        verifyNoInteractions(participantRepository);
    }

    @Test
    void hostLeavingClosesRoomAndRemovesEveryone() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(host));
        when(host.getId()).thenReturn(1L);
        when(roomRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(room));
        when(participantRepository.existsByChatRoomIdAndMemberId(10L, 1L)).thenReturn(true);
        service.leave(10L, 1L);
        assertThat(room.getStatus()).isEqualTo(ChatRoomStatus.CLOSE);
        verify(participantRepository).deleteByChatRoomId(10L);
        verify(participantRepository, never()).deleteByChatRoomIdAndMemberId(any(), any());
    }

    @Test
    void guestLeavingKeepsRoomOpen() {
        prepareGuestJoin();
        when(host.getId()).thenReturn(1L);
        when(participantRepository.existsByChatRoomIdAndMemberId(10L, 2L)).thenReturn(true);
        service.leave(10L, 2L);
        assertThat(room.getStatus()).isEqualTo(ChatRoomStatus.OPEN);
        verify(participantRepository).deleteByChatRoomIdAndMemberId(10L, 2L);
        verify(participantRepository, never()).deleteByChatRoomId(any());
    }

    @Test
    void outsiderCannotReadParticipants() {
        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
        assertThatThrownBy(() -> service.findParticipants(10L, 2L))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("참여자만");
        verify(participantRepository, never()).findByChatRoomIdOrderByJoinedAtAscIdAsc(any());
    }

    @Test
    void outsiderCannotLeave() {
        prepareGuestJoin();
        assertThatThrownBy(() -> service.leave(10L, 2L))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("참여자만");
        verify(participantRepository, never()).deleteByChatRoomId(any());
        verify(participantRepository, never()).deleteByChatRoomIdAndMemberId(any(), any());
    }
}
