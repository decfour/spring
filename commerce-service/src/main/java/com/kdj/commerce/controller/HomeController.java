package com.kdj.commerce.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.session.SessionManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SessionManager sessionManager;

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        Member member = (Member) sessionManager.getSession(request);
        if (member == null) {
            return "home";
        }

        model.addAttribute("member", member);
        return "loginHome";
    }

    @GetMapping("/info")
    public String info() {
        return "info/info";
    }

    @GetMapping("/oasis")
    @ResponseBody
    public String idiot() {
        return "You know, Oasis is the best band in the world";
    }
}
