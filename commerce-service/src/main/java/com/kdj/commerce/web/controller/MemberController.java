package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.domain.purchase.Purchase;
import com.kdj.commerce.domain.review.ItemReview;
import com.kdj.commerce.domain.walk.WalkCourse;
import com.kdj.commerce.service.*;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.dto.member.SignInForm;
import com.kdj.commerce.web.dto.member.SignOnForm;
import com.kdj.commerce.web.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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

@Slf4j
@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final ItemReviewService itemReviewService;
    private final MemberService memberService;
    private final ItemService itemService;
    private final PurchaseService purchaseService;
    private final WalkCourseService walkCourseService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/sign-on")
    public String signOnForm(Model model) {
        model.addAttribute("member", new SignOnForm());

        return "member/signOnForm";
    }

    @PostMapping("/sign-on")
    public String signOn(
            @Valid @ModelAttribute("member") SignOnForm form,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return "member/signOnForm";
        }

        try {
            Member member = Member.create(
                    form.getName(),
                    form.getEmail(),
                    form.getSignInId(),
                    form.getSignInPassword(),
                    MemberType.USER
            );
            memberService.signUp(member);
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("이메일")) {
                result.rejectValue("email", "duplicate", e.getMessage());
            } else if (e.getMessage().contains("아이디")) {
                result.rejectValue("signInId", "duplicate", e.getMessage());
            } else {
                result.reject("signupError", e.getMessage());
            }
            return "member/signOnForm";
        }

        return "redirect:/";
    }

    @GetMapping("/sign-in")
    public String signInForm(
            @RequestParam(defaultValue = "/") String redirectURL,
            Model model
    ) {
        redirectURL = normalizeRedirectUrl(redirectURL);

        model.addAttribute("signInForm", new SignInForm());
        model.addAttribute("redirectURL", redirectURL);

        return "member/signInForm";
    }

    @PostMapping("/sign-in")
    public String signIn(
            @Valid @ModelAttribute SignInForm form,
            BindingResult result,
            @RequestParam(defaultValue = "/") String redirectURL,
            HttpServletResponse response
    ) {
        if (result.hasErrors()) {
            return "member/signInForm";
        }
        redirectURL = normalizeRedirectUrl(redirectURL);

        Member loginMember = memberService.signIn(form.getSignInId(), form.getSignInPassword());

        if (loginMember == null) {
            result.reject("signInError", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "member/signInForm";
        }

        String token = jwtTokenProvider.createToken(loginMember.getEmail());
        log.info("회원 로그인 email={}", loginMember.getEmail());

        Cookie jwtCookie = new Cookie("Authorization", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(1800);
        response.addCookie(jwtCookie);

        return "redirect:" + redirectURL;
    }

    @PostMapping("/sign-out")
    public String signOut(HttpServletResponse response) {
        Cookie cookie = new Cookie("Authorization", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return "redirect:/";
    }

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
        model.addAttribute("member", loginMember);
        model.addAttribute("myCourses", myCourses);

        return "member/myCourse";
    }

    @GetMapping("/my-item")
    public String myItem(@PageableDefault(size = 7, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                         @Login Member loginMember,
                         Model model) {
        Page<Item> myItems = itemService.findByCreatorId(pageable, loginMember.getId());

        model.addAttribute("member", loginMember);
        model.addAttribute("myItems", myItems);

        return "member/myItem";
    }

    @GetMapping("/my-review")
    public String myReview(@PageableDefault(size = 7, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                           @Login Member loginMember,
                           Model model) {
        Page<ItemReview> myReviews = itemReviewService.findByCreatorId(pageable, loginMember.getId());

        model.addAttribute("member", loginMember);
        model.addAttribute("myReviews", myReviews);

        return "member/myReview";
    }

    @GetMapping("/my-order")
    public String myPurchase(@PageableDefault(size = 7, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                          @Login Member loginMember,
                          Model model) {
        Page<Purchase> purchases = purchaseService.findByMemberId(pageable, loginMember.getId());
        model.addAttribute("purchases", purchases);

        return "member/myPurchase";
    }

    private String normalizeRedirectUrl(String redirectURL){
        if(redirectURL.contains(",")){
            redirectURL = redirectURL.split(",")[0];
        }

        if(!redirectURL.startsWith("/")){
            redirectURL="/"+redirectURL;
        }

        return redirectURL;
    }
}
