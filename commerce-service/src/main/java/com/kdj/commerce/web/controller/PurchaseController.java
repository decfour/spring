package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.cart.CartItem;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.exception.PermissionDeniedException;
import com.kdj.commerce.service.CartService;
import com.kdj.commerce.service.ItemService;
import com.kdj.commerce.service.PurchaseService;
import com.kdj.commerce.web.argumentresolver.Login;
import com.kdj.commerce.web.dto.purchase.PurchaseItemForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
public class PurchaseController {
    private final PurchaseService purchaseService;
    private final CartService cartService;
    private final ItemService itemService;

    @GetMapping("/one")
    public String purchaseOne(
            @RequestParam("itemId") Long itemId,
            @RequestParam("itemCount") int itemCount,
            Model model
    ) {
        Item item = itemService.findById(itemId);

        List<PurchaseItemForm> purchaseItems = new ArrayList<>();
        purchaseItems.add(
                new PurchaseItemForm(
                        item.getName(),
                        item.getPrice(),
                        itemCount
                )
        );

        model.addAttribute("purchaseItems", purchaseItems);
        model.addAttribute("totalPrice", item.getPrice() * itemCount);
        model.addAttribute("purchaseType", "ONE");
        model.addAttribute("itemId", itemId);
        model.addAttribute("quantity", itemCount);

        return "purchase/purchaseForm";
    }

    @GetMapping("/cart")
    public String purchaseCart(
            @Login Member loginMember,
            Model model
    ) {
        List<CartItem> cartItems = cartService.findItem(loginMember.getId());

        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        List<PurchaseItemForm> purchaseItems = new ArrayList<>();
        int totalPrice = 0;

        for (CartItem cartItem : cartItems) {
            Item item = cartItem.getItem();

            purchaseItems.add(
                    new PurchaseItemForm(
                            item.getName(),
                            item.getPrice(),
                            cartItem.getQuantity()
                    )
            );

            totalPrice += item.getPrice() * cartItem.getQuantity();
        }

        model.addAttribute("purchaseItems", purchaseItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("purchaseType", "CART");

        return "purchase/purchaseForm";
    }

    @PostMapping("/create")
    public String createPurchase(
            @Login Member loginMember,
            @RequestParam("purchaseType") String purchaseType,
            @RequestParam(value = "itemId", required = false) Long itemId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverAddress") String receiverAddress
    ) {
        if ("ONE".equals(purchaseType)) {
            purchaseService.purchase(
                    loginMember == null ? null : loginMember.getId(),
                    itemId,
                    quantity,
                    receiverName,
                    receiverAddress
            );
        } else if ("CART".equals(purchaseType)) {
            purchaseService.purchaseCart(
                    loginMember == null ? null : loginMember.getId(),
                    receiverName,
                    receiverAddress
            );
        } else {
            throw new IllegalArgumentException("지원하지 않는 주문 방식입니다.");
        }

        return "redirect:/member/my-order";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(
            @Login Member loginMember,
            @PathVariable("id") Long id
    ) {
        purchaseService.cancel(id, loginMember == null ? null : loginMember.getId());

        return "redirect:/member/my-order";
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public String handlePermissionDenied(PermissionDeniedException exception) {
        return "redirect:/member/my-order";
    }
}
