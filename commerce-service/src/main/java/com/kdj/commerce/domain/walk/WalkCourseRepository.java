package com.kdj.commerce.domain.walk;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WalkCourseRepository extends JpaRepository<WalkCourse, Long> {
    @Query(value = """
                    SELECT *
                    FROM walk_course
                    WHERE ST_Distance_Sphere(
                        POINT(start_lng, start_lat),
                        POINT(:longitude, :latitude)
                    ) <= 1000
                    ORDER BY ST_Distance_Sphere(
                        POINT(start_lng, start_lat),
                        POINT(:longitude, :latitude)
                    )
                    """, nativeQuery = true)
    Page<WalkCourse> findNearbyCourses(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            Pageable pageable
    );
}
