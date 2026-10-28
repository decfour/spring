package com.kdj.commerce.service;

import com.kdj.commerce.exception.BusinessConflictException;
import com.kdj.commerce.exception.ResourceNotFoundException;
import com.kdj.commerce.domain.walk.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalkCourseTagService {
    private final WalkCourseTagRepository walkCourseTagRepository;
    private final WalkCourseRepository walkCourseRepository;

    public List<WalkCourseTag> findByCourseId(Long courseId) {
        return walkCourseTagRepository.findByWalkCourseId(courseId);
    }

    @Transactional
    public void create(Long courseId, WalkTag tag) {
        if (walkCourseTagRepository.existsByWalkCourseIdAndTag(courseId, tag)) {
            throw new BusinessConflictException("이미 등록된 태그입니다.");
        }

        WalkCourse walkCourse = walkCourseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 코스입니다 id=" + courseId));

        WalkCourseTag walkCourseTag = WalkCourseTag.create(walkCourse, tag);
        walkCourseTagRepository.save(walkCourseTag);
    }
}
