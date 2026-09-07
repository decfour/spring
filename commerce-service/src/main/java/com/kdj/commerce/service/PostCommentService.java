package com.kdj.commerce.service;

import com.kdj.commerce.domain.community.PostComment;
import com.kdj.commerce.domain.community.PostCommentRepository;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostCommentService {
    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public PostComment findById(Long id) {
        PostComment comment = postCommentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다 id=" + id));
        return comment;
    }

    public Page<PostComment> findByPostId(Long postId, Pageable pageable) {
        return postCommentRepository.findByPostIdWithCreator(postId, pageable);
    }

    @Transactional
    public Long save(Long postId, Long memberId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다 id=" + postId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다 id=" + memberId));

        PostComment comment = PostComment.create(post, member, content);

        return postCommentRepository.save(comment).getId();
    }

    @Transactional
    public void update(Long id, PostComment updateParam) {
        PostComment comment = postCommentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다 id=" + id));

        comment.update(updateParam.getContent());
    }

    @Transactional
    public void delete(Long id) {
        PostComment comment = postCommentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다 id=" + id));

        postCommentRepository.delete(comment);
    }
}
