package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.notice.Notice;
import com.kdj.commerce.domain.review.Review;
import com.kdj.commerce.service.ItemService;
import com.kdj.commerce.service.ReviewService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.form.NoticeSaveForm;
import com.kdj.commerce.web.form.ReviewSaveForm;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/shop/item/{itemId}/review")
public class ReviewController {

    private final ReviewService reviewService;
    private final ItemService itemService;

    @GetMapping
    public String list(@PathVariable Long itemId,
                       @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        Page<Review> reviews = reviewService.findReviewsByItemId(itemId, pageable);

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
                      @Validated @ModelAttribute("reviewForm") ReviewSaveForm form,
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
        reviewService.saveReview(review);

        return "redirect:/shop/item/" + itemId + "/review";
    }

}
