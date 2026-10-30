package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.domain.review.WalkCourseReview;
import com.kdj.commerce.service.WalkCourseReviewService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.dto.review.WalkCourseReviewForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/walk/course/{courseId}/review")
public class WalkCourseReviewController {
    private final WalkCourseReviewService walkCourseReviewService;

    @GetMapping
    public String list(
            @PathVariable Long courseId,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        Page<WalkCourseReview> reviews = walkCourseReviewService.findByCourseId(courseId, pageable);

        model.addAttribute("courseId", courseId);
        model.addAttribute("reviews", reviews);

        return "walk/review/list";
    }

    @GetMapping("/{reviewId}")
    public String detail(
            @Login Member loginMember,
            @PathVariable Long courseId,
            @PathVariable Long reviewId,
            Model model
    ) {
        WalkCourseReview review = walkCourseReviewService.findById(courseId, reviewId);

        model.addAttribute("review", review);
        model.addAttribute("courseId", courseId);
        model.addAttribute("member", loginMember);

        return "walk/review/detail";
    }

    @GetMapping("/add")
    public String addForm(
            @PathVariable Long courseId,
            Model model
    ) {
        walkCourseReviewService.requireCourse(courseId);
        model.addAttribute("reviewForm", new WalkCourseReviewForm());
        model.addAttribute("courseId", courseId);
        model.addAttribute("isEdit", false);

        return "walk/review/form";
    }

    @PostMapping("/add")
    public String add(
            @Login Member loginMember,
            @PathVariable Long courseId,
            @Valid @ModelAttribute("reviewForm") WalkCourseReviewForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("courseId", courseId);

            return "walk/review/form";
        }

        Long reviewId = walkCourseReviewService.save(
                courseId,
                loginMember,
                form.getTitle(),
                form.getContent()
        );

        return "redirect:/walk/course/" + courseId + "/review/" + reviewId;
    }

    @GetMapping("/{reviewId}/edit")
    public String editForm(
            @Login Member loginMember,
            @PathVariable Long courseId,
            @PathVariable Long reviewId,
            Model model
    ) {
        WalkCourseReview review = walkCourseReviewService.findById(courseId, reviewId);

        if (!isOwner(review, loginMember)) {
            log.warn("리뷰 수정 시도 차단 ID={}, 리뷰={}",
                    loginMember == null ? null : loginMember.getId(), review.getId());

            return "redirect:/walk/course/" + courseId + "/review/" + reviewId;
        }

        WalkCourseReviewForm reviewForm = new WalkCourseReviewForm();
        reviewForm.setId(review.getId());
        reviewForm.setTitle(review.getTitle());
        reviewForm.setContent(review.getContent());

        model.addAttribute("reviewForm", reviewForm);
        model.addAttribute("courseId", courseId);
        model.addAttribute("isEdit", true);

        return "walk/review/form";
    }

    @PostMapping("/{reviewId}/edit")
    public String edit(
            @Login Member loginMember,
            @PathVariable Long courseId,
            @PathVariable Long reviewId,
            @Valid @ModelAttribute("reviewForm") WalkCourseReviewForm form,
            BindingResult bindingResult,
            Model model
    ) throws IOException {
        if (!isOwner(walkCourseReviewService.findById(courseId, reviewId), loginMember)) {
            return "redirect:/walk/course/" + courseId + "/review/" + reviewId;
        }
        model.addAttribute("isEdit", true);
        form.setId(reviewId);
        if (bindingResult.hasErrors()) {
            model.addAttribute("courseId", courseId);
            return "walk/review/form";
        }

        walkCourseReviewService.update(
                courseId, reviewId,
                form.getTitle(),
                form.getContent()
        );

        return "redirect:/walk/course/" + courseId + "/review/" + reviewId;
    }

    @PostMapping("/{reviewId}/delete")
    public String delete(
            @Login Member loginMember,
            @PathVariable Long courseId,
            @PathVariable Long reviewId
    ) {
        WalkCourseReview findReview = walkCourseReviewService.findById(courseId, reviewId);

        if (!isOwner(findReview, loginMember) && !isAdmin(loginMember)) {
            log.warn("리뷰 삭제 시도 차단 ID={}, 리뷰={}",
                    loginMember == null ? null : loginMember.getId(), findReview.getId());

            return "redirect:/walk/course/" + courseId + "/review/" + reviewId;
        }

        walkCourseReviewService.delete(courseId, reviewId);

        return "redirect:/walk/course/" + courseId + "/review";
    }

    private boolean isOwner(WalkCourseReview review, Member loginMember) {
        if (loginMember == null)
            return false;
        return review.getCreator() != null && review.getCreator().getId().equals(loginMember.getId());
    }

    private boolean isAdmin(Member loginMember) {
        return loginMember != null && loginMember.getMemberType() == MemberType.ADMIN;
    }
}



