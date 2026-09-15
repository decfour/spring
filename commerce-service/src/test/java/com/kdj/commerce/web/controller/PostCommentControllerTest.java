package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.community.Post;
import com.kdj.commerce.domain.community.PostComment;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.service.PostCommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PostCommentControllerTest {
    private final PostCommentService service = mock(PostCommentService.class);
    private final PostCommentController controller = new PostCommentController(service);

    private Member member(long id, MemberType type) {
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(id);
        when(member.getMemberType()).thenReturn(type);
        return member;
    }

    private void commentCreatedBy(Member creator) {
        Post post = mock(Post.class);
        when(post.getId()).thenReturn(10L);
        PostComment comment = PostComment.create(post, creator, "content");
        when(service.findById(20L)).thenReturn(comment);
    }

    @Test
    @DisplayName("회원 객체가 달라도 회원 번호가 같으면 작성자가 댓글를 삭제할 수 있다")
    void creatorCanDeleteUsingAnotherMemberInstanceWithSameId() {
        commentCreatedBy(member(1L, MemberType.USER));
        assertThat(controller.delete(member(1L, MemberType.USER), 20L))
                .isEqualTo("redirect:/community/post/10");
        verify(service).delete(20L);
    }

    @Test
    @DisplayName("작성자가 아닌 일반 회원은 댓글를 삭제할 수 없다")
    void otherMemberCannotDelete() {
        commentCreatedBy(member(1L, MemberType.USER));
        assertThat(controller.delete(member(2L, MemberType.USER), 20L))
                .isEqualTo("redirect:/community/post/10");
        verify(service, never()).delete(anyLong());
    }

    @Test
    @DisplayName("관리자는 다른 회원이 작성한 댓글를 삭제할 수 있다")
    void administratorCanDelete() {
        commentCreatedBy(member(1L, MemberType.USER));
        assertThat(controller.delete(member(2L, MemberType.ADMIN), 20L))
                .isEqualTo("redirect:/community/post/10");
        verify(service).delete(20L);
    }
}
