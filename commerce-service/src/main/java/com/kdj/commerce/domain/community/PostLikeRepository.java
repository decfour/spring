package com.kdj.commerce.domain.community;

import com.kdj.commerce.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByMemberAndPost(Member member, Post post);

    Optional<PostLike> findByMemberAndPost(Member member, Post post);

    long countByPost(Post post);

    void deleteByPostId(Long postId);

}
