package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.cart.CartItem;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.CartService;
import com.kdj.commerce.web.session.SessionConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    // 장바구니
    @GetMapping
    public String cartList(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                           Model model) {

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

    // 장바구니 아이템 추가
    @PostMapping("/add")
    public String addCart(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                          @RequestParam("itemId") Long itemId,
                          @RequestParam("count") int count) {

        cartService.addCart(loginMember.getId(), itemId, count);

        return "redirect:/shop/item/" + itemId;
    }

    // 장바구니 아이템 제거
    @PostMapping("/item/{cartItemId}/delete")
    public String deleteCartItem(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                                 @PathVariable("cartItemId") Long cartItemId) {

        cartService.deleteCartItem(cartItemId);

        return "redirect:/cart";
    }
}
