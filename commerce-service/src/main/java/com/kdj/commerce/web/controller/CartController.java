package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.cart.CartItem;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.CartService;
import com.kdj.commerce.web.argumentresolver.Login;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public String list(@Login Member loginMember, Model model) {

        // 1. 아이템 목록
        List<CartItem> cartItems = cartService.findCartItem(loginMember.getId());
        model.addAttribute("cartItems", cartItems);

        // 2. 총 가격
        int totalPrice = cartItems.stream()
                .mapToInt(cartItem -> cartItem.getItem().getPrice() * cartItem.getCount())
                .sum();
        model.addAttribute("totalPrice", totalPrice);

        return "cart/cartList";
    }

    @PostMapping("/add")
    public String add(@Login Member loginMember,
                      @RequestParam("itemId") Long itemId,
                      @RequestParam("count") int count) {
        cartService.addCart(loginMember.getId(), itemId, count);

        return "redirect:/shop/item/" + itemId;
    }

    @PostMapping("/item/{cartItemId}/delete")
    public String delete(@Login Member loginMember,
                         @PathVariable("cartItemId") Long cartItemId) {
        cartService.deleteCartItem(cartItemId);

        return "redirect:/cart";
    }
}
