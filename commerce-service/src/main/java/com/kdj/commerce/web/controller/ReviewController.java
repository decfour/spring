package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.review.Review;
import com.kdj.commerce.service.ItemService;
import com.kdj.commerce.service.ReviewService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.form.review.ReviewEditForm;
import com.kdj.commerce.web.form.review.ReviewSaveForm;
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
public class ReviewController {
    private final ReviewService reviewService;
    private final ItemService itemService;

    private boolean isNotOwner(Review review, Member loginMember) {
        if (loginMember == null)
            return true;
        return review.getMember().getId() != loginMember.getId();
    }

    @GetMapping
    public String list(@PathVariable Long itemId,
                       @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        Page<Review> reviews = reviewService.findByItemId(itemId, pageable);

        model.addAttribute("reviews", reviews);
        model.addAttribute("itemId", itemId);

        return "review/reviewList";
    }

    @GetMapping("/{reviewId}")
    public String detail(@PathVariable Long itemId,
                         @PathVariable Long reviewId,
                         Model model) {
        Review review = reviewService.findOne(reviewId);
        model.addAttribute("review", review);
        model.addAttribute("itemId", itemId);

        return "review/review";
    }

    @GetMapping("/add")
    public String addForm(@PathVariable Long itemId,
                          @Login Member loginMember,
                          Model model) {
        model.addAttribute("reviewForm", new ReviewSaveForm());
        model.addAttribute("itemId", itemId);

        return "review/addReviewForm";
    }

    @PostMapping("/add")
    public String add(@PathVariable Long itemId,
                      @Valid @ModelAttribute("reviewForm") ReviewSaveForm form,
                      BindingResult bindingResult,
                      @Login Member loginMember,
                      Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("itemId", itemId);
            return "review/addReviewForm";
        }

        Review review = new Review();
        review.setTitle(form.getTitle());
        review.setContent(form.getContent());

        review.setMember(loginMember);
        Item item = itemService.findOne(itemId);
        review.setItem(item);

        reviewService.save(review);

        return "redirect:/shop/item/" + itemId + "/review";
    }

    @GetMapping("/{reviewId}/edit")
    public String editForm(@PathVariable Long itemId,
                           @PathVariable Long reviewId,
                           @Login Member loginMember,
                           Model model) {
        Review review = reviewService.findOne(reviewId);

        if (isNotOwner(review, loginMember)) {
            return "redirect:/shop/item/" + itemId + "/review/" + reviewId;
        }

        ReviewEditForm reviewEditForm = new ReviewEditForm();
        reviewEditForm.setId(review.getId());
        reviewEditForm.setTitle(review.getTitle());
        reviewEditForm.setContent(review.getContent());

        model.addAttribute("reviewForm", reviewEditForm);
        model.addAttribute("itemId", itemId);

        return "review/editReviewForm";
    }

    @PostMapping("/{reviewId}/edit")
    public String edit(@PathVariable Long itemId,
                       @PathVariable Long reviewId,
                       @Login Member loginMember,
                       @Valid @ModelAttribute ReviewEditForm reviewEditForm,
                       BindingResult bindingResult,
                       Model model) throws IOException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("itemId", itemId);
            return "review/editReviewForm";
        }

        Review findReview = reviewService.findOne(reviewId);

        Review updateParam = new Review();
        updateParam.setId(reviewEditForm.getId());
        updateParam.setTitle(reviewEditForm.getTitle());
        updateParam.setContent(reviewEditForm.getContent());

        reviewService.update(reviewId, updateParam);

        return "redirect:/shop/item/" + itemId + "/review/" + reviewId;
    }


}
