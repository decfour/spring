package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.cart.CartItem;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.order.Order;
import com.kdj.commerce.exception.NotEnoughStockException;
import com.kdj.commerce.service.CartService;
import com.kdj.commerce.service.ItemService;
import com.kdj.commerce.service.OrderService;
import com.kdj.commerce.web.session.SessionConst;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final ItemService itemService;

    // 주문 내역
    @GetMapping("/list")
    public String list(@SessionAttribute(name = SessionConst.LOGIN_MEMBER) Member loginMember,
                       Model model) {
        List<Order> orders = orderService.findOrdersByMember(loginMember.getId());
        model.addAttribute("orders", orders);

        return "order/orderList";
    }

    // 바로 구매
    @PostMapping("/one")
    public String orderOne(@SessionAttribute(name = SessionConst.LOGIN_MEMBER) Member loginMember,
                           @RequestParam("itemId") Long itemId,
                           @RequestParam("count") int count,
                           Model model) {
        Item item = itemService.findOne(itemId);

        List<OrderItemDto> orderItems = new ArrayList<>();
        orderItems.add(new OrderItemDto(item.getName(), item.getPrice(), count));

        model.addAttribute("orderItems", orderItems);
        model.addAttribute("totalPrice", item.getPrice() * count);
        model.addAttribute("orderType", "ONE");
        model.addAttribute("itemId", itemId);
        model.addAttribute("count", count);

        return "order/orderForm";
    }

    // 전체 구매
    @PostMapping("/cart")
    public String orderCart(@SessionAttribute(name = SessionConst.LOGIN_MEMBER) Member loginMember,
                            Model model) {

        List<CartItem> cartItems = cartService.findCartItem(loginMember.getId());
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        List<OrderItemDto> orderItems = new ArrayList<>();
        int totalPrice = 0;
        for (CartItem cartItem : cartItems) {
            orderItems.add(new OrderItemDto(cartItem.getItem().getName(), cartItem.getItem().getPrice(), cartItem.getCount()));
            totalPrice += cartItem.getItem().getPrice() * cartItem.getCount();
        }

        model.addAttribute("orderItems", orderItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("orderType", "CART");

        return "order/orderForm";
    }

    @PostMapping("/create")
    public String createOrder(
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER) Member loginMember,
            @RequestParam("orderType") String orderType,
            @RequestParam(value = "itemId", required = false) Long itemId,
            @RequestParam(value = "count", required = false) Integer count,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("address") String address) {

        // 1. 넘어온 배송지 정보와 함께 주문 등록 비즈니스 로직 처리
        if ("ONE".equals(orderType)) {
            // 주신 코드 기반 흐름대로 처리하되, 필요하다면 배송지 파라미터를 추가해 줍니다.
            orderService.order(loginMember.getId(), itemId, count);
        } else {
            List<CartItem> cartItems = cartService.findCartItem(loginMember.getId());
            orderService.cartOrder(loginMember.getId(), cartItems);
        }

        // 2. 주문 완료 시 비동기 응답이 아니라, 곧바로 주문 내역 화면으로 리다이렉트 시킵니다.
        return "redirect:/order/list";
    }


    // 주문 취소
    @PostMapping("/{orderId}/cancel")
    public String cancel(@SessionAttribute(name = SessionConst.LOGIN_MEMBER) Member loginMember,
                         @PathVariable("orderId") Long orderId) {

        orderService.cancelOrder(orderId);

        return "redirect:/order/list";
    }

    @ExceptionHandler(NotEnoughStockException.class)
    public String handleNotEnoughStockException(NotEnoughStockException e, Model model) {

        model.addAttribute("errorMessage", e.getMessage());

        return "common/alertAndRedirect";
    }

    @Data
    @AllArgsConstructor
    static class OrderItemDto {
        private String itemName;
        private int orderPrice;
        private int count;
    }

    @Data
    static class OrderCreateRequest {
        private String receiverName;
        private String address;
        private String orderType;
        private Long itemId;
        private Integer count;
    }

}
