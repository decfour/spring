package com.kdj.commerce.service;

import com.kdj.commerce.domain.community.Comment;
import com.kdj.commerce.domain.community.CommentRepository;
import com.kdj.commerce.domain.community.Post;
import com.kdj.commerce.domain.community.PostRepository;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public Comment findOne(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다 id=" + id));
        return comment;
    }

    public Page<Comment> findByPostId(Long postId, Pageable pageable) {
        return commentRepository.findByPostIdWithWriter(postId, pageable);
    }

    @Transactional
    public Long save(Long postId, Long memberId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다 id=" + postId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다 id=" + memberId));

        Comment comment = Comment.create(post, member, content);

        return commentRepository.save(comment).getId();
    }

    @Transactional
    public void update(Long id, Comment updateParam) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다 id=" + id));

        comment.update(updateParam.getContent());
    }

    @Transactional
    public void delete(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다 id=" + id));

        commentRepository.delete(comment);
    }
}
