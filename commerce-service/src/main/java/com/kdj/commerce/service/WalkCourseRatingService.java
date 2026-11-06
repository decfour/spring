package com.kdj.commerce.service;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.walk.*;
import com.kdj.commerce.exception.BusinessConflictException;
import com.kdj.commerce.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalkCourseRatingService {
    private final WalkCourseRatingRepository ratings;
    private final EntityManager entityManager;

    public Integer findScore(Long courseId, Long memberId) {
        if (memberId == null) return null;

        return ratings
                .findByCourseIdAndMemberId(courseId, memberId)
                .map(WalkCourseRating::getScore).orElse(null);
    }

    @Transactional
    public void rate(Long courseId, Member member, Integer score) {
        if (score == null || score < 1 || score > 5)
            throw new IllegalArgumentException("별점은 1~5점으로 선택해주세요.");

        var course = lockCourse(courseId);
        if (course.getCreator() != null && course.getCreator().getId().equals(member.getId()))
            throw new BusinessConflictException("본인의 코스에는 별점을 남길 수 없습니다.");

        var rating = ratings.findByCourseIdAndMemberId(courseId, member.getId())
                .orElseGet(() -> {
                    course.addRating();
                    return WalkCourseRating.create(course, member, score);
                });

        rating.update(score);

        ratings.save(rating);
    }

    @Transactional
    public void cancel(Long courseId, Member member) {
        var course = lockCourse(courseId);

        ratings.findByCourseIdAndMemberId(courseId, member.getId()).ifPresent(rating -> {
            ratings.delete(rating);
            course.removeRating();
        });
    }

    private WalkCourse lockCourse(Long courseId) {
        var course = entityManager.find(WalkCourse.class, courseId, LockModeType.PESSIMISTIC_WRITE);
        if (course == null)
            throw new ResourceNotFoundException("산책 코스를 찾을 수 없습니다.");

        return course;
    }
}
