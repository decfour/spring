package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.domain.order.Order;
import com.kdj.commerce.domain.review.Review;
import com.kdj.commerce.domain.walk.WalkCourse;
import com.kdj.commerce.service.*;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.dto.member.LoginForm;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.web.dto.member.registerForm;
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

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final ReviewService reviewService;
    private final MemberService memberService;
    private final ItemService itemService;
    private final OrderService orderService;
    private final WalkCourseService walkCourseService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("member", new registerForm());

        return "member/registerForm";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("member") registerForm form,
                           BindingResult result) {
        if (result.hasErrors()) {
            return "member/registerForm";
        }

        try {
            Member member = Member.create(
                    form.getUsername(),
                    form.getEmail(),
                    form.getLoginId(),
                    form.getLoginPassword(),
                    MemberType.USER
            );
            memberService.signUp(member);
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("이메일")) {
                result.rejectValue("email", "duplicate", e.getMessage());
            } else if (e.getMessage().contains("아이디")) {
                result.rejectValue("loginId", "duplicate", e.getMessage());
            } else {
                result.reject("signupError", e.getMessage());
            }
            return "member/registerForm";
        }

        return "redirect:/";
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(defaultValue = "/") String redirectURL,
                            Model model) {
        redirectURL = normalizeRedirectUrl(redirectURL);

        model.addAttribute("loginForm", new LoginForm());
        model.addAttribute("redirectURL", redirectURL);

        return "member/loginForm";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginForm form,
                        BindingResult result,
                        @RequestParam(defaultValue = "/") String redirectURL,
                        HttpServletResponse response) {
        if (result.hasErrors()) {
            return "member/loginForm";
        }
        redirectURL = normalizeRedirectUrl(redirectURL);

        Member loginMember = memberService.login(form.getLoginId(), form.getLoginPassword());

        // 로그인 실패
        if (loginMember == null) {
            result.reject("loginError", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "member/loginForm";
        }

        // 로그인 성공 -> JWT 토큰 생성
        String token = jwtTokenProvider.createToken(loginMember.getEmail());
        log.info("회원 로그인 email={}", loginMember.getEmail());

        Cookie jwtCookie = new Cookie("Authorization", token);
        jwtCookie.setHttpOnly(true);        // 자바스크립트로 해킹 못 하게 보안 설정
        jwtCookie.setPath("/");             // 모든 경로에서 이 쿠키를 들고 오도록 설정
        jwtCookie.setMaxAge(1800);          // 유효시간 30분
        response.addCookie(jwtCookie);

        return "redirect:" + redirectURL;
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
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
        Page<WalkCourse> myCourses = walkCourseService.findByMemberId(pageable, loginMember.getId());
        model.addAttribute("member", loginMember);
        model.addAttribute("myCourses", myCourses);

        return "member/myCourse";
    }

    @GetMapping("/my-item")
    public String myItem(@PageableDefault(size = 7, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                         @Login Member loginMember,
                         Model model) {
        Page<Item> myItems = itemService.findByCreatedBy(pageable, loginMember.getId());

        model.addAttribute("member", loginMember);
        model.addAttribute("myItems", myItems);

        return "member/myItem";
    }

    @GetMapping("/my-review")
    public String myReview(@PageableDefault(size = 7, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                           @Login Member loginMember,
                           Model model) {
        Page<Review> myReviews = reviewService.findByMemberId(pageable, loginMember.getId());

        model.addAttribute("member", loginMember);
        model.addAttribute("myReviews", myReviews);

        return "member/myReview";
    }

    @GetMapping("/my-order")
    public String myOrder(@PageableDefault(size = 7, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                          @Login Member loginMember,
                          Model model) {
        Page<Order> orders = orderService.findByMemberId(pageable, loginMember.getId());
        model.addAttribute("orders", orders);

        return "member/myOrder";
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
