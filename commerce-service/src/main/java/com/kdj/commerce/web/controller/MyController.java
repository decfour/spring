package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.MyService;
import com.kdj.commerce.service.ItemService;
import com.kdj.commerce.service.ItemReviewService;
import com.kdj.commerce.service.PurchaseService;
import com.kdj.commerce.service.WalkCourseService;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.purchase.Purchase;
import com.kdj.commerce.domain.review.ItemReview;
import com.kdj.commerce.domain.walk.WalkCourse;
import com.kdj.commerce.web.argumentresolver.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MyController {
    private final MyService service;
    private final ItemService itemService;
    private final ItemReviewService itemReviewService;
    private final PurchaseService purchaseService;
    private final WalkCourseService walkCourseService;

    @GetMapping("/my-page")
    public String myPage(@Login Member loginMember,
                         Model model) {
        model.addAttribute("member", loginMember);

        return "member/myPage";
    }

    @GetMapping("/my-course")
    public String myCourse(@PageableDefault(size = 7, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                           @Login Member loginMember,
                           Model model
                            ) {
        Page<WalkCourse> myCourses = walkCourseService.findByCreatorId(pageable, loginMember.getId());
        model.addAttribute("myCourses", myCourses);
        model.addAttribute("tab", "my-course");

        return "member/myCourse";
    }

    @GetMapping("/my-shop")
    public String myShop(@PageableDefault(size = 7, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                         @Login Member loginMember,
                         Model model) {
        Page<Item> myItems = itemService.findByCreatorId(pageable, loginMember.getId());

        model.addAttribute("myItems", myItems);
        model.addAttribute("tab", "item");

        return "member/myShop";
    }

    @GetMapping("/my-review")
    public String myReview(@PageableDefault(size = 7, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                           @Login Member loginMember,
                           Model model) {
        Page<ItemReview> myReviews = itemReviewService.findByCreatorId(pageable, loginMember.getId());

        model.addAttribute("myReviews", myReviews);
        model.addAttribute("tab", "review");

        return "member/myShop";
    }

    @GetMapping("/my-order")
    public String myPurchase(@PageableDefault(size = 7, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                          @Login Member loginMember,
                          Model model) {
        Page<Purchase> purchases = purchaseService.findByMemberId(pageable, loginMember.getId());
        model.addAttribute("purchases", purchases);
        model.addAttribute("tab", "order");

        return "member/myShop";
    }

    @GetMapping("/my-{activity:pin|chat|course-review}")
    public String course(@PathVariable String activity, @Login Member member,
                         @PageableDefault(size = 7, sort = "id", direction = Sort.Direction.DESC) Pageable page,
                         Model model) {
        String tab = "my-" + activity;
        Page<MyService.Entry> entries = switch (activity) {
            case "pin" -> service.pins(member.getId(), page);
            case "chat" -> service.chats(member.getId(), page);
            default -> service.reviews(member.getId(), page);
        };

        return activity(model, "Course", tab, "/member/" + tab, entries);
    }

    @GetMapping("/my-talk")
    public String talk(@Login Member member, @RequestParam(defaultValue = "post") String tab,
                       @PageableDefault(size = 7, sort = "id", direction = Sort.Direction.DESC) Pageable page, Model model) {
        boolean comment = "comment".equals(tab);
        String selected = comment ? "comment" : "post";

        return activity(model, "Talk", selected, "/member/my-talk?tab=" + selected,
                comment ? service.comments(member.getId(), page) : service.posts(member.getId(), page));
    }

    private String activity(Model model, String section, String tab, String url, Page<MyService.Entry> entries) {
        model.addAttribute("tab", tab);
        model.addAttribute("listUrl", url);
        model.addAttribute("entries", entries);

        return section.equals("Course") ? "member/myCourse" : "member/myTalk";
    }
}
