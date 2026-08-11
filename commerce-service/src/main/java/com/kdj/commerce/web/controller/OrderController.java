package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.cart.CartItem;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.order.Order;
import com.kdj.commerce.exception.NotEnoughStockException;
import com.kdj.commerce.service.CartService;
import com.kdj.commerce.service.ItemService;
import com.kdj.commerce.service.OrderService;
import com.kdj.commerce.web.argumentresolver.Login;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;
    private final CartService cartService;
    private final ItemService itemService;

    @GetMapping("/list")
    public String list(@Login Member loginMember,
                       Model model) {
        List<Order> orders = orderService.findByMemberId(loginMember.getId());
        model.addAttribute("orders", orders);

        return "order/orderList";
    }

    @PostMapping("/one")
    public String orderOne(@Login Member loginMember,
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

    @PostMapping("/cart")
    public String orderCart(@Login Member loginMember,
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
            @Login Member loginMember,
            @RequestParam("orderType") String orderType,
            @RequestParam(value = "itemId", required = false) Long itemId,
            @RequestParam(value = "count", required = false) Integer count,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("address") String address) {
        if ("ONE".equals(orderType)) {
            orderService.order(loginMember.getId(), itemId, count);
        }
        else {
            List<CartItem> cartItems = cartService.findCartItem(loginMember.getId());
            orderService.orderCart(loginMember.getId(), cartItems);
        }

        return "redirect:/order/list";
    }

    @PostMapping("/{orderId}/cancel")
    public String cancel(@Login Member loginMember,
                         @PathVariable("orderId") Long orderId) {
        orderService.cancel(loginMember.getId(), orderId);

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
