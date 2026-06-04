package com.kdj.commerce.web.controller;

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

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    // 회원가입 화면 진입
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("member", new MemberSaveForm()); // 💡 폼 객체로 변경

        return "member/registerForm";
    }

    // 회원가입 처리
    @PostMapping("/register")
    public String registerForm(@Valid @ModelAttribute("member") MemberSaveForm form,
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
            result.reject("duplicateEmail", e.getMessage());
            return "member/registerForm";
        }

        return "redirect:/";
    }

    // 로그인
    @GetMapping("/login")
    public String login(@RequestParam(defaultValue = "/") String redirectURL,
                        Model model) {
        model.addAttribute("loginForm", new LoginForm());
        model.addAttribute("redirectURL", redirectURL);
        return "member/loginForm";
    }

    @PostMapping("/login")
    public String loginForm(@Valid @ModelAttribute LoginForm form,
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

    // 로그아웃
    @PostMapping("/logout")
    public String logoutV2(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // 쿠키 제거
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setMaxAge(0); // 유효기간을 0초로 세팅해서 브라우저가 바로 삭제하게 만듦
        cookie.setPath("/"); // 내 서버 전체 경로에 적용
        response.addCookie(cookie);

        return "redirect:/";
    }

}
