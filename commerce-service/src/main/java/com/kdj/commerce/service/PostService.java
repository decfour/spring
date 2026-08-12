package com.kdj.commerce.service;

import com.kdj.commerce.domain.community.*;
import com.kdj.commerce.domain.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;

    // Fetch (Post + Member)
    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAllWithWriter(pageable);
    }

    // Fetch (Post + Member)
    public Page<Post> findHit(Pageable pageable) {
        return postRepository.findHitWithWriter(20, pageable);
    }

    public Post findOne(Long id) {
        return postRepository.findByIdWithWriter(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다 id=" + id));
    }

    @Transactional
    public Long save(String title, String content, Member member) {
        Post post = Post.create(title, content, member);
        return postRepository.save(post).getId();
    }

    @Transactional
    public void update(Long id, String title, String content) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다 id=" + id));

        post.update(title, content);
    }

    @Transactional
    public void delete(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다 id = " + id));

        commentRepository.deleteByPostId(id);
        postLikeRepository.deleteByPostId(id);
        postRepository.delete(post);
    }

    @Transactional
    public void increaseViewCount(Long id) {
        int updatedCount = postRepository.increaseViewCount(id);

        if (updatedCount == 0) {
            throw new IllegalArgumentException("존재하지 않는 게시글입니다 id=" + id);
        }
    }

    @Transactional
    public void like(Long postId, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다 id=" + postId));

        if (postLikeRepository.existsByMemberAndPost(member, post)) {
            throw new IllegalStateException("이미 추천한 게시글입니다");
        }

        postLikeRepository.save(PostLike.create(member, post));

        postRepository.increaseLikeCount(postId);
    }
}
