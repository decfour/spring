package com.kdj.commerce.domain.walk;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface WalkCourseRepository extends JpaRepository<WalkCourse, Long> {
    @Query(value = """
                        SELECT w
                        FROM WalkCourse w
                        WHERE w.startLat BETWEEN :minLat AND :maxLat
                          AND w.startLng BETWEEN :minLng AND :maxLng
                          AND function('ST_Distance_Sphere',
                              function('POINT', w.startLng, w.startLat),
                              function('POINT', :longitude, :latitude)
                          ) <= 1000
                        ORDER BY function('ST_Distance_Sphere',
                            function('POINT', w.startLng, w.startLat),
                            function('POINT', :longitude, :latitude)
                        )
                        """)
    Page<WalkCourse> findNearbyCourses(@Param("latitude") Double latitude,
                                       @Param("longitude") Double longitude,
                                       @Param("minLat") Double minLat,
                                       @Param("maxLat") Double maxLat,
                                       @Param("minLng") Double minLng,
                                       @Param("maxLng") Double maxLng,
                                       Pageable pageable);

    Page<WalkCourse> findByCreatorIdOrderByCreatedAtDesc(Long id, Pageable pageable);

    List<WalkCourse> findTop30ByOrderByAverageRatingDescRatingCountDescCreatedAtDescIdDesc();
}





