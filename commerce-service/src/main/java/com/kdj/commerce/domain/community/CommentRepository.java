package com.kdj.commerce.domain.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query(
            value = """
                    select c
                    from Comment c
                    join fetch c.writer
                    where c.post.id = :postId
                    order by c.createdDate asc, c.id desc
                    """,
            countQuery = """
                    select count(c)
                    from Comment c
                    where c.post.id = :postId
                    """)
    Page<Comment> findByPostIdWithWriter(@Param("postId") Long postId, Pageable pageable);

    Page<Comment> findByPostIdOrderByCreatedDateAsc(Long id, Pageable pageable);

    void deleteByPostId (Long id);
}
