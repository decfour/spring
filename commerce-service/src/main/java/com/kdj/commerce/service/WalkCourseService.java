package com.kdj.commerce.service;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.walk.WalkCourse;
import com.kdj.commerce.domain.walk.WalkCourseRepository;
import com.kdj.commerce.web.dto.walk.WalkRouteResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
