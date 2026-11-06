package com.kdj.commerce.domain.community;

import com.kdj.commerce.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByMemberAndPost(Member member, Post post);

    void deleteByPostId(Long postId);

}
