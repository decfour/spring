package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.domain.review.ItemReview;
import com.kdj.commerce.service.ItemReviewService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.dto.review.ItemReviewForm;
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
@RequestMapping("/shop/item/{itemId}/review")
public class ItemReviewController {
    private final ItemReviewService itemReviewService;

    @GetMapping
    public String list(
            @PathVariable Long itemId,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        Page<ItemReview> reviews = itemReviewService.findByItemId(itemId, pageable);

        model.addAttribute("itemId", itemId);
        model.addAttribute("reviews", reviews);

        return "review/list";
    }

    @GetMapping("/{reviewId}")
    public String detail(
            @Login Member loginMember,
            @PathVariable Long itemId,
            @PathVariable Long reviewId,
            Model model
    ) {
        ItemReview review = itemReviewService.findById(reviewId);

        model.addAttribute("review", review);
        model.addAttribute("itemId", itemId);
        model.addAttribute("member", loginMember);

        return "review/detail";
    }

    @GetMapping("/add")
    public String addForm(
            @PathVariable Long itemId,
            Model model
    ) {
        model.addAttribute("reviewForm", new ItemReviewForm());
        model.addAttribute("itemId", itemId);
        model.addAttribute("isEdit", false);

        return "review/form";
    }

    @PostMapping("/add")
    public String add(
            @Login Member loginMember,
            @PathVariable Long itemId,
            @Valid @ModelAttribute("reviewForm") ItemReviewForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("itemId", itemId);

            return "review/form";
        }

        Long reviewId = itemReviewService.save(
                itemId,
                loginMember,
                form.getTitle(),
                form.getContent()
        );

        return "redirect:/shop/item/" + itemId + "/review/" + reviewId;
    }

    @GetMapping("/{reviewId}/edit")
    public String editForm(
            @Login Member loginMember,
            @PathVariable Long itemId,
            @PathVariable Long reviewId,
            Model model
    ) {
        ItemReview review = itemReviewService.findById(reviewId);

        if (!isOwner(review, loginMember)) {
            log.warn("리뷰 수정 시도 차단 ID={}, 리뷰={}",
                    loginMember == null ? null : loginMember.getId(), review.getId());

            return "redirect:/shop/item/" + itemId + "/review/" + reviewId;
        }

        ItemReviewForm reviewForm = new ItemReviewForm();
        reviewForm.setId(review.getId());
        reviewForm.setTitle(review.getTitle());
        reviewForm.setContent(review.getContent());

        model.addAttribute("reviewForm", reviewForm);
        model.addAttribute("itemId", itemId);
        model.addAttribute("isEdit", true);

        return "review/form";
    }

    @PostMapping("/{reviewId}/edit")
    public String edit(
            @Login Member loginMember,
            @PathVariable Long itemId,
            @PathVariable Long reviewId,
            @Valid @ModelAttribute("reviewForm") ItemReviewForm form,
            BindingResult bindingResult,
            Model model
    ) throws IOException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("itemId", itemId);
            return "review/form";
        }

        ItemReview findReview = itemReviewService.findById(reviewId);
        if (!isOwner(findReview, loginMember)) {
            log.warn("리뷰 수정 시도 차단 ID={}, 리뷰={}",
                    loginMember == null ? null : loginMember.getId(), findReview.getId());

            return "redirect:/shop/item/" + itemId + "/review/" + reviewId;
        }

        itemReviewService.update(
                reviewId,
                form.getTitle(),
                form.getContent()
        );

        return "redirect:/shop/item/" + itemId + "/review/" + reviewId;
    }

    @PostMapping("/{reviewId}/delete")
    public String delete(
            @Login Member loginMember,
            @PathVariable Long itemId,
            @PathVariable Long reviewId
    ) {
        ItemReview findReview = itemReviewService.findById(reviewId);

        if (!isOwner(findReview, loginMember) && !isAdmin(loginMember)) {
            log.warn("리뷰 삭제 시도 차단 ID={}, 리뷰={}",
                    loginMember == null ? null : loginMember.getId(), findReview.getId());

            return "redirect:/shop/item/" + itemId + "/review/" + reviewId;
        }

        itemReviewService.delete(reviewId);

        return "redirect:/shop/item/" + itemId + "/review";
    }

    private boolean isOwner(ItemReview review, Member loginMember) {
        if (loginMember == null)
            return false;
        return review.getCreator().getId().equals(loginMember.getId());
    }

    private boolean isAdmin(Member loginMember) {
        return loginMember != null && loginMember.getMemberType() == MemberType.ADMIN;
    }
}
