package com.kdj.commerce.controller;

import com.kdj.commerce.domain.member.LoginForm;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.MemberService;
import com.kdj.commerce.session.SessionConst;
import com.kdj.commerce.session.SessionManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("member")
public class MemberController {
    private final MemberService memberService;
    private final SessionManager sessionManager;

    @Autowired
    public MemberController(MemberService memberService, SessionManager sessionManager) {
        this.memberService = memberService;
        this.sessionManager = sessionManager;
    }

    // 회원가입
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("member", new Member());
        return "member/registerForm";
    }

    @PostMapping("/register")
    public String registerForm(@Valid @ModelAttribute Member member, BindingResult result) {
        if (result.hasErrors()) {
            return "member/registerForm";
        }
        memberService.join(member);
        List<Member> members = memberService.findMembers();
        log.info("members={}", members);
        return "redirect:/";
    }

    // 로그인
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "member/loginForm";
    }

    @PostMapping("/login")
    public String loginForm(@Valid @ModelAttribute LoginForm form,
                            BindingResult result,
                            HttpServletRequest request) {
        if (result.hasErrors()) {
            return "member/loginForm";
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

        return "redirect:/";
    }

    // 로그아웃
    @PostMapping("/logout")
    public String logoutV2(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return "redirect:/";
    }

}
