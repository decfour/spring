package com.kdj.commerce.domain.walk;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WalkCoursePinRepository extends JpaRepository<WalkCoursePin, Long> {
    @EntityGraph(attributePaths = "course")
    Page<WalkCoursePin> findByCreatorId(Long memberId, Pageable pageable);

    @EntityGraph(attributePaths = "creator")
    List<WalkCoursePin> findByCourseIdOrderByIdDesc(Long courseId);
    Optional<WalkCoursePin> findByIdAndCourseId(Long id, Long courseId);
}
