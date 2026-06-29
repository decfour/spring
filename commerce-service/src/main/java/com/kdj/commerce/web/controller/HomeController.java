package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.session.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final SessionManager sessionManager;

    @GetMapping("/")
    public String home(@Login Member loginMember, Model model) {
        if (loginMember == null) {
            return "guestHome";
        }
        model.addAttribute("member", loginMember);

        return "userHome";
    }

    @GetMapping("/info")
    public String info() {
        return "info/info";
    }
}
