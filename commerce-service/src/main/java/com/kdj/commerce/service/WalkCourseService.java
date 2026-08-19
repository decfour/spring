package com.kdj.commerce.service;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.walk.WalkCourse;
import com.kdj.commerce.domain.walk.WalkCourseLike;
import com.kdj.commerce.domain.walk.WalkCourseLikeRepository;
import com.kdj.commerce.domain.walk.WalkCourseRepository;
import com.kdj.commerce.web.dto.walk.WalkCourseResponse;
import com.kdj.commerce.web.dto.walk.WalkRouteResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalkCourseService {
    private final WalkCourseRepository walkCourseRepository;
    private final KakaoRouteService kakaoRouteService;
    private final WalkCourseLikeRepository walkCourseLikeRepository;

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
            Integer duration) {
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

    @Transactional
    public Long update(Long id, String name, String review) {
        WalkCourse walkCourse = walkCourseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("찾을 수 없습니다 id=" + id));
        walkCourse.update(name, review);

        return walkCourse.getId();
    }

    @Transactional
    public void delete(Long id) {
        WalkCourse walkCourse = walkCourseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("찾을 수 없습니다 id=" + id));
        walkCourseLikeRepository.deleteByWalkCourseId(id);
        walkCourseRepository.delete(walkCourse);
    }

    public WalkCourse findOne(long id) {
        WalkCourse walkCourse = walkCourseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("찾을 수 없습니다"));
        return walkCourse;
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

    public WalkRouteResult findRoute(Double startLat,
                                     Double startLng,
                                     Double endLat,
                                     Double endLng) {
        return kakaoRouteService.findWalkRoute(startLat, startLng, endLat, endLng);
    }

    @Transactional
    public int like(Long courseId, Member member) {
         WalkCourse walkCourse = walkCourseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다 id=" + courseId));

        if (walkCourseLikeRepository.existsByMemberAndWalkCourse(member, walkCourse)) {
            throw new IllegalStateException("이미 추천한 코스입니다");
        }

        walkCourseLikeRepository.save(WalkCourseLike.create(member, walkCourse));

        walkCourseRepository.increaseLikeCount(courseId);

        return walkCourseRepository.findById(courseId)
                .orElseThrow()
                .getLikeCount();
    }
}
