package com.kdj.commerce.domain.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query(
            value = """
                select p
                from Post p
                join fetch p.writer
                """,
            countQuery = """
                select count(p)
                from Post p
                """)
    Page<Post> findAllWithWriter(Pageable pageable);

    @Query(
            value = """
                select p
                from Post p
                join fetch p.writer
                where p.likeCount >= :likeCount
                order by p.likeCount desc
                """,
            countQuery = """
                select count(p)
                from Post p
                where p.likeCount >= :likeCount
                """)
    Page<Post> findHitWithWriter(@Param("likeCount") int likeCount, Pageable pageable);

    @Query("""
                select p
                from Post p
                join fetch p.writer
                where p.id = :id
                """)
    Optional<Post> findByIdWithWriter(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    int increaseViewCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    int increaseLikeCount(@Param("id") Long id);
}