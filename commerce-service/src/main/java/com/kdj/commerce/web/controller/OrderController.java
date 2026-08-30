package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.cart.CartItem;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.CartService;
import com.kdj.commerce.service.ItemService;
import com.kdj.commerce.service.OrderService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.dto.order.OrderItemForm;
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

    @GetMapping("/one")
    public String orderOne(
            @RequestParam("itemId") Long itemId,
            @RequestParam("itemCount") int itemCount,
            Model model
    ) {
        Item item = itemService.findById(itemId);

        List<OrderItemForm> orderItems = new ArrayList<>();
        orderItems.add(
                new OrderItemForm(
                        item.getName(),
                        item.getPrice(),
                        itemCount
                )
        );

        model.addAttribute("orderItems", orderItems);
        model.addAttribute("totalPrice", item.getPrice() * itemCount);
        model.addAttribute("orderType", "ONE");
        model.addAttribute("itemId", itemId);
        model.addAttribute("count", itemCount);

        return "order/orderForm";
    }

    @GetMapping("/cart")
    public String orderCart(
            @Login Member loginMember,
            Model model
    ) {
        List<CartItem> cartItems = cartService.findItem(loginMember.getId());

        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        List<OrderItemForm> orderItems = new ArrayList<>();
        int totalPrice = 0;

        for (CartItem cartItem : cartItems) {
            Item item = cartItem.getItem();

            orderItems.add(
                    new OrderItemForm(
                            item.getName(),
                            item.getPrice(),
                            cartItem.getCount()
                    )
            );

            totalPrice += item.getPrice() * cartItem.getCount();
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
            @RequestParam("receiverAddress") String receiverAddress
    ) {
        if ("ONE".equals(orderType)) {
            orderService.order(
                    loginMember.getId(),
                    itemId,
                    count,
                    receiverName,
                    receiverAddress
            );
        } else {
            List<CartItem> cartItems = cartService.findItem(loginMember.getId());

            orderService.orderCart(
                    loginMember.getId(),
                    cartItems,
                    receiverName,
                    receiverAddress
            );
        }

        return "redirect:/member/my-order";
    }

    @PostMapping("/{orderId}/cancel")
    public String cancel(
            @Login Member loginMember,
            @PathVariable("orderId") Long orderId
    ) {
        orderService.cancel(loginMember.getId(), orderId);

        return "redirect:/member/my-order";
    }
}
