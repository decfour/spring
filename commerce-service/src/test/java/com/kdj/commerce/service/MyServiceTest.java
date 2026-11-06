package com.kdj.commerce.service;

import com.kdj.commerce.domain.chat.*;
import com.kdj.commerce.domain.community.*;
import com.kdj.commerce.domain.member.*;
import com.kdj.commerce.domain.review.WalkCourseReview;
import com.kdj.commerce.domain.walk.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MyServiceTest {
    @Autowired EntityManager em;
    @Autowired MyService service;

    @Test
    @DisplayName("내 활동은 본인 기록과 해당 상세 경로만 조회한다")
    void activityQueriesOnlyReturnTheMembersOwnRecordsAndPreserveDestinations() {
        String suffix = java.util.UUID.randomUUID().toString().substring(0, 8);
        var owner = Member.create("작성자", "my-"+suffix+"@example.com", "my-"+suffix, "password", MemberType.USER);
        var other = Member.create("다른회원", "other-"+suffix+"@example.com", "other-"+suffix, "password", MemberType.USER);
        em.persist(owner); em.persist(other);
        var course = WalkCourse.create(owner, "산책 코스", "설명", 37.5, 127.0, 37.51, 127.01, "[[127,37.5],[127.01,37.51]]", 1200, 900);
        em.persist(course);
        var post = Post.create("게시글", "본문", owner); em.persist(post);
        var room = ChatRoom.create(course, owner, "참여한 방"); em.persist(room);
        var pin = WalkCoursePin.create(course, owner, "내 쪽지", "내용", 37.5, 127.0); em.persist(pin);
        em.persist(WalkCoursePin.create(course, other, "다른 쪽지", "내용", 37.5, 127.0));
        var review = WalkCourseReview.create("내 후기", "후기", course, owner); em.persist(review);
        em.persist(WalkCourseReview.create("다른 후기", "후기", course, other));
        em.persist(ChatMember.create(room, owner));
        var otherRoom = ChatRoom.create(course, other, "다른 방"); em.persist(otherRoom);
        em.persist(ChatMember.create(otherRoom, other));
        em.persist(Post.create("다른 글", "본문", other));
        em.persist(PostComment.create(post, owner, "내 댓글"));
        em.persist(PostComment.create(post, other, "다른 댓글"));
        em.flush(); em.clear();
        var page = PageRequest.of(0, 7, Sort.by(Sort.Direction.DESC, "id"));

        assertThat(service.pins(owner.getId(), page).getContent()).singleElement().satisfies(entry -> {
            assertThat(entry.title()).isEqualTo("내 쪽지");
            assertThat(entry.url()).isEqualTo("/walk/course/"+course.getId()+"?pin="+pin.getId());
        });
        assertThat(service.chats(owner.getId(), page).getContent()).singleElement()
                .satisfies(entry -> assertThat(entry.url()).isEqualTo("/walk/course/"+course.getId()+"/chat/"+room.getId()));
        assertThat(service.reviews(owner.getId(), page).getContent()).singleElement()
                .satisfies(entry -> assertThat(entry.url()).endsWith("/review/"+review.getId()));
        assertThat(service.posts(owner.getId(), page).getContent()).singleElement()
                .satisfies(entry -> assertThat(entry.title()).isEqualTo("게시글"));
        assertThat(service.comments(owner.getId(), page).getContent()).singleElement().satisfies(entry -> {
            assertThat(entry.title()).isEqualTo("내 댓글");
            assertThat(entry.url()).isEqualTo("/community/post/"+post.getId());
        });
        assertThat(service.pins(owner.getId(), PageRequest.of(1, 1)).getContent()).isEmpty();
    }
}
