package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.review.Review;
import com.kdj.commerce.service.ItemService;
import com.kdj.commerce.service.ReviewService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.form.member.LoginForm;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.MemberService;
import com.kdj.commerce.web.form.member.MemberSaveForm;
import com.kdj.commerce.web.session.SessionConst;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("member", new MemberSaveForm()); // 💡 폼 객체로 변경

        return "member/registerForm";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("member") MemberSaveForm form,
                               BindingResult result) {
        if (result.hasErrors()) {
            return "member/registerForm";
        }

        try {
            Member member = Member.createMember(
                    form.getUsername(),
                    form.getEmail(),
                    form.getLoginId(),
                    form.getLoginPassword()
            );
            memberService.join(member);
        } catch (IllegalStateException e) {
            result.reject("이메일 중복", e.getMessage());
            return "member/registerForm";
        }

        return "redirect:/";
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(defaultValue = "/") String redirectURL,
                        Model model) {
        model.addAttribute("loginForm", new LoginForm());
        model.addAttribute("redirectURL", redirectURL);

        return "member/loginForm";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginForm form,
                            BindingResult result,
                            @RequestParam(defaultValue = "/") String redirectURL,
                            HttpServletRequest request) {
        if (result.hasErrors()) {
            return "member/loginForm";
        }

        if (redirectURL.contains(",")) {
            redirectURL = redirectURL.split(",")[0];
        }

        Member loginMember = memberService.login(form.getLoginId(), form.getLoginPassword());

        // 로그인 실패
        if (loginMember == null) {
            result.reject("loginError", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "member/loginForm";
        }

        // 로그인 성공
        HttpSession session = request.getSession();
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);
        log.info("loginUser={}", loginMember);

        return "redirect:" + redirectURL;
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        // 쿠키 제거
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setMaxAge(0); // 유효기간을 0초로 세팅
        cookie.setPath("/"); // 서버 전체 경로 적용
        response.addCookie(cookie);

        return "redirect:/";
    }

    @GetMapping("/my-page")
    public String myPage(@Login Member loginMember,
                         Model model) {
        model.addAttribute("member", loginMember);

        return "member/myPage";
    }

    @GetMapping("/my-page/my-item")
    public String myItems(@Login Member loginMember,
                          Model model) {
        List<Item> myItems = itemService.findItemsByCreatedBy(loginMember.getId());

        model.addAttribute("member", loginMember);
        model.addAttribute("myItems", myItems);

        return "member/myItem";
    }

    @GetMapping("/my-page/my-review")
    public String myReviews(@Login Member loginMember,
                          Model model) {
        List<Review> myReviews = reviewService.findItemsByMemberId(loginMember.getId());

        model.addAttribute("member", loginMember);
        model.addAttribute("myReviews", myReviews);

        return "member/myReview";
    }
}
