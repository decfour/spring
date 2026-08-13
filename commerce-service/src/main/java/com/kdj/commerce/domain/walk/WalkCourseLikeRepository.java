package com.kdj.commerce.domain.walk;

import com.kdj.commerce.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalkCourseLikeRepository extends JpaRepository<WalkCourseLike, Long> {
    boolean existsByMemberAndWalkCourse(Member member, WalkCourse walkCourse);

    void deleteByWalkCourseId(Long walkCourseId);
}
