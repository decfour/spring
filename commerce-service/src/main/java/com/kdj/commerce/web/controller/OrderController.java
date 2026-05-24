package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.order.Order;
import com.kdj.commerce.service.OrderService;
import com.kdj.commerce.web.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("shop/order")
    public String order(@RequestParam("itemId") Long itemId,
                        @RequestParam("count") int count,
                        HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "redirect:/login";
        }

        Member loginMember = (Member) session.getAttribute(SessionConst.LOGIN_MEMBER);
        Long memberId = loginMember.getId();
        orderService.order(memberId, itemId, count);

        return "redirect:/orders";
    }

    @GetMapping("/orders")
    public String orderList(Model model) {
        // 전체 주문 내역을 조회해서 model에 담아 템플릿으로 넘깁니다.
        List<Order> orders = orderService.findOrders();
        model.addAttribute("orders", orders); // ◀️ 이 이름으로 타임리프에서 꺼내 쓸 겁니다!

        return "order/orderList";
    }

    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancelOrder(orderId);

        return "redirect:/orders";
    }

}
