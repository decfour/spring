package com.kdj.commerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ItemController {

    @GetMapping("/shop")
    public String shop() {
        return "shop/shop";
    }
}
