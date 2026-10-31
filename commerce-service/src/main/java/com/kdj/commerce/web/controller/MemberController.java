package com.kdj.commerce.web.controller;

import com.kdj.commerce.exception.DuplicateMemberException;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.service.MemberService;
import com.kdj.commerce.web.dto.member.SignInForm;
import com.kdj.commerce.web.dto.member.SignUpForm;
import com.kdj.commerce.web.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute("member", new SignUpForm());

        return "member/signUpForm";
    }

    @PostMapping("/sign-up")
    public String signUp(
            @Valid @ModelAttribute("member") SignUpForm form,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return "member/signUpForm";
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
        } catch (DuplicateMemberException e) {
            String field = switch (e.getField()) {
                case EMAIL -> "email";
                case SIGN_IN_ID -> "signInId";
            };
            result.rejectValue(field, "duplicate", e.getMessage());
            return "member/signUpForm";
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
