package com.kdj.commerce.domain.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    @Query("""
            select c
            from CommunityPost c
            where c.likeCount >= :likeCount
            order by c.likeCount desc
            """)
    Page<CommunityPost> findHits(@Param("likeCount") int likeCount,
                                 Pageable pageable);
}
