package com.kdj.commerce.domain.review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface WalkCourseReviewRepository extends JpaRepository<WalkCourseReview, Long> {
    Page<WalkCourseReview> findByCourseId(Pageable pageable, Long courseId);
    Optional<WalkCourseReview> findByIdAndCourseId(Long id, Long courseId);
}
