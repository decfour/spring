package com.kdj.commerce.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.domain.walk.*;
import com.kdj.commerce.exception.*;
import com.kdj.commerce.web.dto.walk.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalkCoursePinService {
    private final WalkCourseRepository courses;
    private final WalkCoursePinRepository pins;
    private final ObjectMapper objectMapper;

    public List<WalkCoursePinResponse> list(Long courseId, Member member) {
        requireCourse(courseId);

        return pins.findByCourseIdOrderByIdDesc(courseId).stream()
                .map(pin -> WalkCoursePinResponse.from(pin, canDelete(pin, member))).toList();
    }

    @Transactional
    public WalkCoursePinResponse create(Long courseId, Member member, WalkCoursePinRequest request) {
        if (member == null) throw new AuthenticationRequiredException("로그인이 필요합니다.");
        var course = requireCourse(courseId);
        if (!isNearRoute(course.getRouteData(), request.latitude(), request.longitude())) {
            throw new InvalidRequestException("핀은 코스 경로에서 30m 이내에 등록해주세요.");
        }

        var pin = pins.save(WalkCoursePin.create(course, member, request.title().trim(), request.content().trim(),
                request.latitude(), request.longitude()));

        return WalkCoursePinResponse.from(pin, true);
    }

    @Transactional
    public void delete(Long courseId, Long pinId, Member member) {
        var pin = pins.findByIdAndCourseId(pinId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("핀을 찾을 수 없습니다."));
        if (!canDelete(pin, member)) throw new PermissionDeniedException("핀을 삭제할 권한이 없습니다.");

        pins.delete(pin);
    }

    private WalkCourse requireCourse(Long id) {
        return courses.findById(id).orElseThrow(() -> new ResourceNotFoundException("코스를 찾을 수 없습니다."));
    }

    private boolean canDelete(WalkCoursePin pin, Member member) {
        return member != null && (member.getMemberType() == MemberType.ADMIN
                || pin.getCreator().getId().equals(member.getId()));
    }

    boolean isNearRoute(String routeData, double latitude, double longitude) {
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)) return false;
        try {
            JsonNode route = objectMapper.readTree(routeData);
            if (!route.isArray() || route.size() < 2) return false;
            double scale = Math.cos(Math.toRadians(latitude));
            double[] previous = null;
            double minimum = Double.POSITIVE_INFINITY;
            for (JsonNode point : route) {
                if (!point.isArray() || point.size() < 2 || !point.get(0).isNumber() || !point.get(1).isNumber()) return false;
                double lng = point.get(0).asDouble(), lat = point.get(1).asDouble();
                if (!Double.isFinite(lng) || !Double.isFinite(lat) || Math.abs(lng) > 180 || Math.abs(lat) > 90) return false;
                double[] current = {Math.toRadians(lng - longitude) * 6371008.8 * scale,
                        Math.toRadians(lat - latitude) * 6371008.8};
                if (previous != null) {
                    double dx = current[0] - previous[0], dy = current[1] - previous[1];
                    double length = dx * dx + dy * dy;
                    double t = length == 0 ? 0 : Math.max(0, Math.min(1,
                            -(previous[0] * dx + previous[1] * dy) / length));
                    minimum = Math.min(minimum, Math.hypot(previous[0] + t * dx, previous[1] + t * dy));
                }
                previous = current;
            }

            return minimum <= 30;
        } catch (Exception e) {
            return false;
        }
    }
}
