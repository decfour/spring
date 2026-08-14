package com.kdj.commerce.domain.walk;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalkCourseTagRepository extends JpaRepository<WalkCourseTag, Long> {
    List<WalkCourseTag> findByWalkCourseId(Long walkCourseId);
    boolean existsByWalkCourseIdAndTag(Long walkCourseId, WalkTag tag);
}
