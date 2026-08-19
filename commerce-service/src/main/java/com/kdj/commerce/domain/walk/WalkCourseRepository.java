package com.kdj.commerce.domain.walk;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalkCourseRepository extends JpaRepository<WalkCourse, Long> {
    @Query(value = """
                        SELECT *
                        FROM walk_course
                        WHERE start_lat BETWEEN :minLat AND :maxLat
                          AND start_lng BETWEEN :minLng AND :maxLng
                          AND ST_Distance_Sphere(
                              POINT(start_lng, start_lat),
                              POINT(:longitude, :latitude)
                          ) <= 1000
                        ORDER BY ST_Distance_Sphere(
                            POINT(start_lng, start_lat),
                            POINT(:longitude, :latitude)
                        )
                        """,
                        nativeQuery = true)
    Page<WalkCourse> findNearbyCourses(@Param("latitude") Double latitude,
                                       @Param("longitude") Double longitude,
                                       @Param("minLat") Double minLat,
                                       @Param("maxLat") Double maxLat,
                                       @Param("minLng") Double minLng,
                                       @Param("maxLng") Double maxLng,
                                       Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE WalkCourse w SET w.likeCount = w.likeCount + 1 WHERE w.id = :id")
    int increaseLikeCount(@Param("id") Long id);
}





