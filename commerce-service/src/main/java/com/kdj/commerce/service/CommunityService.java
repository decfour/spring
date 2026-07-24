package com.kdj.commerce.service;

import com.kdj.commerce.domain.community.CommunityPost;
import com.kdj.commerce.domain.community.CommunityPostRepository;
import com.kdj.commerce.domain.notice.Notice;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommunityService {
    private final CommunityPostRepository communityPostRepository;

    public Page<CommunityPost> findAll(Pageable pageable) {
        return communityPostRepository.findAll(pageable);
    }

    public CommunityPost findOne(Long id) {
        return communityPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않습니다 ID=" + id));
    }

    public Page<CommunityPost> findHit(Pageable pageable) {
        return communityPostRepository.findHits(20, pageable);
    }

    @Transactional
    public Long save(CommunityPost communityPost) {
        CommunityPost savedCommunityPost = communityPostRepository.save(communityPost);

        return savedCommunityPost.getId();
    }

    @Transactional
    public CommunityPost increaseViewCount(Long id) {
        CommunityPost post = communityPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다. id=" + id));
        post.increaseViewCount();

        return post;
    }

    @Transactional
    public CommunityPost increaseLikeCount(Long id) {
        CommunityPost post = communityPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다. id=" + id));
        post.increaseLikeCount();

        return post;
    }



}
