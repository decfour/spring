package com.kdj.commerce.service;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.review.WalkCourseReview;
import com.kdj.commerce.domain.review.WalkCourseReviewRepository;
import com.kdj.commerce.domain.walk.WalkCourseRepository;
import com.kdj.commerce.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalkCourseReviewService {
    private final WalkCourseReviewRepository reviewRepository;
    private final WalkCourseRepository courseRepository;

    public WalkCourseReview findById(Long courseId, Long reviewId) {
        return reviewRepository.findByIdAndCourseId(reviewId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 코스의 후기를 찾을 수 없습니다."));
    }

    public void requireCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("산책 코스를 찾을 수 없습니다.");
        }
    }

    public Page<WalkCourseReview> findByCourseId(Long courseId, Pageable pageable) {
        requireCourse(courseId);
        return reviewRepository.findByCourseId(pageable, courseId);
    }

    @Transactional
    public Long save(Long courseId, Member member, String title, String content) {
        var course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("산책 코스를 찾을 수 없습니다."));
        return reviewRepository.save(WalkCourseReview.create(title, content, course, member)).getId();
    }

    @Transactional
    public void update(Long courseId, Long reviewId, String title, String content) {
        findById(courseId, reviewId).update(title, content);
    }

    @Transactional
    public void delete(Long courseId, Long reviewId) {
        reviewRepository.delete(findById(courseId, reviewId));
    }
}
