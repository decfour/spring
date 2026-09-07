package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.community.Post;
import com.kdj.commerce.domain.community.PostComment;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.service.PostCommentService;
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
    void creatorCanDeleteUsingAnotherMemberInstanceWithSameId() {
        commentCreatedBy(member(1L, MemberType.USER));
        assertThat(controller.delete(member(1L, MemberType.USER), 20L))
                .isEqualTo("redirect:/community/post/10");
        verify(service).delete(20L);
    }

    @Test
    void otherMemberCannotDelete() {
        commentCreatedBy(member(1L, MemberType.USER));
        assertThat(controller.delete(member(2L, MemberType.USER), 20L))
                .isEqualTo("redirect:/community/post/10");
        verify(service, never()).delete(anyLong());
    }

    @Test
    void administratorCanDelete() {
        commentCreatedBy(member(1L, MemberType.USER));
        assertThat(controller.delete(member(2L, MemberType.ADMIN), 20L))
                .isEqualTo("redirect:/community/post/10");
        verify(service).delete(20L);
    }
}
