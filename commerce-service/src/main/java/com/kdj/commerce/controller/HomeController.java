package com.kdj.commerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
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
