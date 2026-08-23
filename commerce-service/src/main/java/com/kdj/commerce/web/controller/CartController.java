package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.cart.CartItem;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.CartService;
import com.kdj.commerce.web.argumentresolver.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public String list(
            @Login Member loginMember,
            Model model
    ) {
        List<CartItem> cartItems = cartService.findItem(loginMember.getId());

        int totalPrice = cartItems.stream()
                .mapToInt(cartItem -> cartItem.getItem().getPrice() * cartItem.getCount())
                .sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);

        return "cart/cartList";
    }

    @PostMapping("/item/add")
    public String addItem(
            @Login Member loginMember,
            @RequestParam("itemId") Long itemId,
            @RequestParam("itemCount") int itemCount
    ) {
        cartService.addItem(loginMember.getId(), itemId, itemCount);

        return "redirect:/shop/item/" + itemId;
    }

    @PostMapping("/item/{cartItemId}/delete")
    public String deleteItem(
            @Login Member loginMember,
            @PathVariable("cartItemId") Long cartItemId
    ) {
        cartService.deleteItem(loginMember.getId(), cartItemId);

        return "redirect:/cart";
    }
}
