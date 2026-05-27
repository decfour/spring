package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.cart.CartItem;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.order.Order;
import com.kdj.commerce.service.CartService;
import com.kdj.commerce.service.OrderService;
import com.kdj.commerce.web.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    // 바로 구매
    @PostMapping("/shop/order")
    public String order(@RequestParam("itemId") Long itemId,
                        @RequestParam("count") int count,
                        @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember) {

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        orderService.order(loginMember.getId(), itemId, count);
        return "redirect:/orders";
    }

    // 전체 구매
    @PostMapping("/order/cart")
    public String orderCart(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember) {
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        List<CartItem> cartItems = cartService.findCartItem(loginMember.getId());

        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        orderService.cartOrder(loginMember.getId(), cartItems);

        cartService.clearCart(loginMember.getId());

        return "redirect:/orders";
    }

    @GetMapping("/orders")
    public String orderList(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                            Model model) {
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        List<Order> orders = orderService.findOrders();
        model.addAttribute("orders", orders); // ◀️ 이 이름으로 타임리프에서 꺼내 쓸 겁니다!

        return "order/orderList";
    }

    // 주문 취소
    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId,
                              @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember) {
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        orderService.cancelOrder(orderId);
        return "redirect:/orders";
    }

}
