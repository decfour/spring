package com.kdj.commerce.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.session.SessionConst;
import com.kdj.commerce.session.SessionManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SessionManager sessionManager;

    @GetMapping("/")
    public String homeLogin(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                            Model model) {
        // 세션 X
        if (loginMember == null) {
            return "guestHome";
        }

        // 세션 O
        model.addAttribute("member", loginMember);
        return "userHome";
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
