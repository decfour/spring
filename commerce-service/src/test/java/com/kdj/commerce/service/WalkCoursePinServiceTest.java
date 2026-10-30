package com.kdj.commerce.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdj.commerce.domain.member.*;
import com.kdj.commerce.domain.walk.*;
import com.kdj.commerce.exception.*;
import com.kdj.commerce.web.dto.walk.WalkCoursePinRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalkCoursePinServiceTest {
    private final WalkCourseRepository courses = mock(WalkCourseRepository.class);
    private final WalkCoursePinRepository pins = mock(WalkCoursePinRepository.class);
    private final WalkCoursePinService service = new WalkCoursePinService(courses, pins, new ObjectMapper());
    private final String route = "[[127.0,37.5],[127.01,37.5]]";

    @Test
    void checksSegmentsBetweenDistantNodesAndThirtyMeterBoundary() {
        double degreePerMeter = 180 / (Math.PI * 6371008.8);
        assertThat(service.isNearRoute(route, 37.5 + 29.9 * degreePerMeter, 127.005)).isTrue();
        assertThat(service.isNearRoute(route, 37.5 + 30.1 * degreePerMeter, 127.005)).isFalse();
        assertThat(service.isNearRoute(route, 37.5, 127.005)).isTrue();
        // The extension of a segment is not part of the route.
        assertThat(service.isNearRoute(route, 37.5, 127.02)).isFalse();
        assertThat(service.isNearRoute("[[127,37.5],[127,37.5]]", 37.5, 127)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"invalid", "null", "[]", "[[127,37.5]]", "[[127,37.5],[null,37.5]]",
            "[[127,37.5],[181,37.5]]", "{}"})
    void invalidRoutesCannotAcceptPins(String data) {
        assertThat(service.isNearRoute(data, 37.5, 127)).isFalse();
    }

    @Test
    void savesPinOnCourseAndRejectsFarAwayOrAnonymousSubmissions() {
        var member = member(1L, MemberType.USER);
        var course = WalkCourse.create(member, "course", "", 37.5, 127.0, 37.5, 127.01, route, 1000, 600);
        when(courses.findById(10L)).thenReturn(Optional.of(course));
        when(pins.save(any())).thenAnswer(call -> call.getArgument(0));
        var response = service.create(10L, member, new WalkCoursePinRequest(" Bench ", " Nice view ", 37.5, 127.005));
        assertThat(response.title()).isEqualTo("Bench");
        assertThat(response.deletable()).isTrue();
        verify(pins).save(argThat(pin -> pin.getCourse() == course && pin.getCreator() == member));
        assertThatThrownBy(() -> service.create(10L, member,
                new WalkCoursePinRequest("Far", "Away", 37.6, 127.005))).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.create(10L, null,
                new WalkCoursePinRequest("Title", "Content", 37.5, 127.005))).isInstanceOf(AuthenticationRequiredException.class);
        verify(pins, times(1)).save(any());
    }

    @Test
    void deletionRequiresMatchingCourseAndOwnerOrAdmin() {
        var pin = WalkCoursePin.create(null, member(1L, MemberType.USER), "Title", "Content", 37.5, 127);
        when(pins.findByIdAndCourseId(20L, 10L)).thenReturn(Optional.of(pin));
        assertThatThrownBy(() -> service.delete(10L, 20L, member(2L, MemberType.USER)))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> service.delete(10L, 20L, null)).isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> service.delete(11L, 20L, member(1L, MemberType.USER)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(pins, never()).delete(any());
        service.delete(10L, 20L, member(1L, MemberType.USER));
        service.delete(10L, 20L, member(2L, MemberType.ADMIN));
        verify(pins, times(2)).delete(pin);
    }

    private Member member(long id, MemberType type) {
        var member = mock(Member.class);
        when(member.getId()).thenReturn(id);
        when(member.getName()).thenReturn("Walker");
        when(member.getMemberType()).thenReturn(type);
        return member;
    }
}
