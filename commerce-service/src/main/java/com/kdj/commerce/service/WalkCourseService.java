package com.kdj.commerce.service;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.walk.WalkCourse;
import com.kdj.commerce.domain.walk.WalkCourseRepository;
import com.kdj.commerce.web.dto.walk.WalkCourseResponse;
import com.kdj.commerce.web.dto.walk.WalkRouteResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalkCourseService {
    private final WalkCourseRepository walkCourseRepository;
    private final KakaoRouteService kakaoRouteService;

    @Transactional
    public Long save(
            Member member,
            String name,
            String review,
            Double startLat,
            Double startLng,
            Double endLat,
            Double endLng,
            String routeData,
            Integer distance,
            Integer duration
    ) {
        WalkCourse walkCourse = WalkCourse.create(
                member,
                name,
                review,
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

    public WalkCourse findOne(long id) {
        WalkCourse walkCourse = walkCourseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("찾을 수 없습니다"));
        return walkCourse;
    }

    public Page<WalkCourseResponse> findNearbyCourses(
            Pageable pageable,
            Double latitude,
            Double longitude
    ) {
        return walkCourseRepository
                .findNearbyCourses(latitude, longitude, pageable)
                .map(WalkCourseResponse::from);
    }

    public WalkRouteResult findRoute(
            Double startLat,
            Double startLng,
            Double endLat,
            Double endLng
    ) {
        return kakaoRouteService.findWalkRoute(
                startLat,
                startLng,
                endLat,
                endLng
        );
    }
}
