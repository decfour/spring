package com.kdj.commerce.domain.walk;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WalkCourseRatingRepository extends JpaRepository<WalkCourseRating, Long> {
    Optional<WalkCourseRating> findByCourseIdAndMemberId(Long courseId, Long memberId);
}
