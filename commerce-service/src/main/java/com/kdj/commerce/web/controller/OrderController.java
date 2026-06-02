package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.cart.CartItem;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.order.Order;
import com.kdj.commerce.exception.NotEnoughStockException;
import com.kdj.commerce.service.CartService;
import com.kdj.commerce.service.OrderService;
import com.kdj.commerce.web.session.SessionConst;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    @GetMapping("/list")
    public String orderList(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                            Model model) {
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        List<Order> orders = orderService.findOrdersByMember(loginMember.getId());
        model.addAttribute("orders", orders);

        return "order/orderList";
    }

    // 바로 구매
    @PostMapping("/one")
    public String order(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                        @RequestParam("itemId") Long itemId,
                        @RequestParam("count") int count) {

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        orderService.order(loginMember.getId(), itemId, count);
        return "redirect:/order/list";
    }

    // 전체 구매
    @PostMapping("/cart")
    public String orderCart(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember) {
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        List<CartItem> cartItems = cartService.findCartItem(loginMember.getId());

        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        orderService.cartOrder(loginMember.getId(), cartItems);

        return "redirect:/order/list";
    }

    // 주문 취소
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                              @PathVariable("orderId") Long orderId) {
        if (loginMember == null) {
            return "redirect:/member/login";
        }

        orderService.cancelOrder(orderId);
        return "redirect:/order/list";
    }

    @ExceptionHandler(NotEnoughStockException.class)
    public String handleNotEnoughStockException(NotEnoughStockException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());

        return "common/alertAndRedirect";
    }

}
