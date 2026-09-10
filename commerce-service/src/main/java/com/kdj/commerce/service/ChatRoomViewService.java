package com.kdj.commerce.service;

import com.kdj.commerce.domain.chat.*;
import com.kdj.commerce.domain.walk.WalkCourse;
import com.kdj.commerce.domain.walk.WalkCourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomViewService {
    private final ChatRoomRepository rooms;
    private final ChatMemberRepository members;
    private final WalkCourseRepository courses;
    private final ChatRoomService service;

    public record Course(Long id, String title, Integer distance, Integer duration) {}
    public record Room(Long id, String title, String hostName, Long hostId,
                       boolean open, long count, boolean joined) {}
    public record Participant(Long id, String name) {}
    public record Detail(Course course, Room room, List<Participant> participants) {}

    public Course course(Long courseId) {
        return courseView(courses.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 산책 코스입니다.")));
    }

    public Page<Room> list(Long courseId, Long memberId, boolean mine, Pageable pageable) {
        Page<ChatRoom> page = mine ? rooms.findJoinedRooms(courseId, memberId, pageable)
                : rooms.findByWalkCourseIdAndStatusOrderByCreatedAtDescIdDesc(courseId, ChatRoomStatus.OPEN, pageable);
        return page.map(room -> roomView(room, memberId));
    }

    public void requireCourse(Long courseId, Long roomId) {
        ChatRoom room = rooms.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));
        if (!room.getWalkCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("해당 코스의 채팅방이 아닙니다.");
        }
    }

    public Detail detail(Long courseId, Long roomId, Long memberId) {
        requireCourse(courseId, roomId);
        ChatRoom room = service.findById(roomId, memberId);
        List<Participant> participants = members.findByChatRoomIdOrderByJoinedAtAscIdAsc(roomId)
                .stream().map(m -> new Participant(m.getMember().getId(), m.getMember().getName())).toList();
        return new Detail(courseView(room.getWalkCourse()), roomView(room, memberId), participants);
    }

    private Course courseView(WalkCourse course) {
        return new Course(course.getId(), course.getTitle(), course.getDistance(), course.getDuration());
    }

    private Room roomView(ChatRoom room, Long memberId) {
        return new Room(room.getId(), room.getTitle(), room.getHost().getName(), room.getHost().getId(),
                room.getStatus() == ChatRoomStatus.OPEN, members.countByChatRoomId(room.getId()),
                members.existsByChatRoomIdAndMemberId(room.getId(), memberId));
    }
}
