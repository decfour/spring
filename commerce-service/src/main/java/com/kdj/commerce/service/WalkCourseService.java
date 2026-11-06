package com.kdj.commerce.service;

import com.kdj.commerce.exception.ResourceNotFoundException;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.walk.WalkCourse;
import com.kdj.commerce.domain.walk.WalkCourseRepository;
import com.kdj.commerce.web.dto.walk.WalkCourseResponse;
import com.kdj.commerce.web.dto.walk.WalkRouteResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalkCourseService {
    private final WalkCourseRepository walkCourseRepository;
    private final KakaoRouteService kakaoRouteService;

    public WalkCourse findById(long id) {
        WalkCourse walkCourse = walkCourseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("찾을 수 없습니다"));
        return walkCourse;
    }

    public Page<WalkCourse> findByCreatorId(Pageable pageable, Long id) {
        return walkCourseRepository.findByCreatorIdOrderByCreatedAtDesc(id, pageable);
    }

    public Page<WalkCourseResponse> findNearbyCourses(Pageable pageable,
                                                      Double latitude,
                                                      Double longitude) {
        // 바운싱 박스 (1km)
        double latDelta = 1.0 / 111.0;
        double lngDelta = 1.0 / (111.0 * Math.cos(Math.toRadians(latitude)));

        double minLat = latitude - latDelta;
        double maxLat = latitude + latDelta;
        double minLng = longitude - lngDelta;
        double maxLng = longitude + lngDelta;

        return walkCourseRepository.findNearbyCourses(
                        latitude,
                        longitude,
                        minLat,
                        maxLat,
                        minLng,
                        maxLng,
                        pageable)
                .map(WalkCourseResponse::from);
    }

    public Page<WalkCourse> findBest(Pageable pageable) {
        var courses = walkCourseRepository.findTop30ByOrderByAverageRatingDescRatingCountDescCreatedAtDescIdDesc();
        if (pageable.isUnpaged()) {
            return new PageImpl<>(courses);
        }
        int from = (int) Math.min(pageable.getOffset(), courses.size());
        int to = from + Math.min(pageable.getPageSize(), courses.size() - from);

        return new PageImpl<>(courses.subList(from, to), pageable, courses.size());
    }

    @Transactional
    public Long save(
            Member member,
            String title,
            String content,
            Double startLat,
            Double startLng,
            Double endLat,
            Double endLng,
            String routeData,
            Integer distance,
            Integer duration) {
        WalkCourse walkCourse = WalkCourse.create(
                member,
                title,
                content,
                startLat,
                startLng,
                endLat,
                endLng,
                routeData,
                distance,
                duration
        );

        walkCourseRepository.save(walkCourse);

        return walkCourse.getId();
    }

    @Transactional
    public void delete(Long id) {
        WalkCourse walkCourse = walkCourseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("찾을 수 없습니다 id=" + id));
        walkCourseRepository.delete(walkCourse);
    }

    public WalkRouteResult findRoute(Double startLat,
                                     Double startLng,
                                     Double endLat,
                                     Double endLng) {
        return kakaoRouteService.findWalkRoute(startLat, startLng, endLat, endLng);
    }
}
