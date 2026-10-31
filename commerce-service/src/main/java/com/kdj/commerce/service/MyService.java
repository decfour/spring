package com.kdj.commerce.service;

import com.kdj.commerce.domain.chat.ChatMemberRepository;
import com.kdj.commerce.domain.community.PostRepository;
import com.kdj.commerce.domain.community.PostCommentRepository;
import com.kdj.commerce.domain.review.WalkCourseReviewRepository;
import com.kdj.commerce.domain.walk.WalkCoursePinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyService {
    private final WalkCoursePinRepository pins;
    private final ChatMemberRepository chats;
    private final WalkCourseReviewRepository reviews;
    private final PostRepository posts;
    private final PostCommentRepository comments;

    public record Entry(String title, String context, String url, LocalDateTime date) {}

    public Page<Entry> pins(Long memberId, Pageable page) {
        return pins.findByCreatorId(memberId, page).map(pin -> new Entry(pin.getTitle(), pin.getCourse().getTitle(),
                "/walk/course/" + pin.getCourse().getId() + "?pin=" + pin.getId(), pin.getCreatedAt()));
    }

    public Page<Entry> chats(Long memberId, Pageable page) {
        return chats.findByMemberId(memberId, page).map(join -> {
            var room = join.getChatRoom();
            return new Entry(room.getTitle(), room.getWalkCourse().getTitle(),
                    "/walk/course/" + room.getWalkCourse().getId() + "/chat/" + room.getId(), join.getJoinedAt());
        });
    }

    public Page<Entry> reviews(Long memberId, Pageable page) {
        return reviews.findByCreatorId(memberId, page).map(review -> new Entry(review.getTitle(), review.getCourse().getTitle(),
                "/walk/course/" + review.getCourse().getId() + "/review/" + review.getId(), review.getCreatedAt()));
    }

    public Page<Entry> posts(Long memberId, Pageable page) {
        return posts.findByCreatorId(memberId, page).map(post -> new Entry(post.getTitle(), "", "/community/post/" + post.getId(), post.getCreatedAt()));
    }

    public Page<Entry> comments(Long memberId, Pageable page) {
        return comments.findByCreatorId(memberId, page).map(comment -> new Entry(comment.getContent(), comment.getPost().getTitle(),
                "/community/post/" + comment.getPost().getId(), comment.getCreatedAt()));
    }
}
