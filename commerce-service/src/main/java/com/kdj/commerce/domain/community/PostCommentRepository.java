package com.kdj.commerce.domain.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    @Query(
            value = """
                    select c
                    from PostComment c
                    join fetch c.creator
                    where c.post.id = :postId
                    order by c.createdAt asc, c.id desc
                    """,
            countQuery = """
                    select count(c)
                    from PostComment c
                    where c.post.id = :postId
                    """)
    Page<PostComment> findByPostIdWithCreator(@Param("postId") Long postId, Pageable pageable);

    Page<PostComment> findByPostIdOrderByCreatedAtAsc(Long id, Pageable pageable);

    void deleteByPostId (Long id);
}
