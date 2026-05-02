package com.kdj.commerce.controller;

import com.kdj.commerce.domain.Member;
import com.kdj.commerce.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;

@Controller
@RequestMapping("members")
public class MemberController {
    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("members", memberService.findMembers());
        return "members/memberList";
    }

    // 로그인
    @GetMapping("/login")
    public String loginForm() {
        return "members/loginForm"; // loginForm.html로 연결
    }

    // 회원가입
    @GetMapping("/new")
    public String createForm() {
        return "members/createMemberForm";
    }

    @PostMapping("/new")
    public void create(@ModelAttribute Member member, HttpServletResponse response) throws IOException {
        try {
            memberService.join(member);

            response.setContentType("text/html; charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script>alert('회원가입이 완료되었습니다!'); location.href='/';</script>");
            out.flush();
        }
        catch (IllegalStateException e){
            response.setContentType("text/html; charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script>alert('" + e.getMessage() + "'); history.back();</script>");
            out.flush();
        }
    }

}
