package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.cart.CartItem;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.CartService;
import com.kdj.commerce.web.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    // 장바구니
    @GetMapping
    public String cartList(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "redirect:/login";
        }
        Member loginMember = (Member) session.getAttribute(SessionConst.LOGIN_MEMBER);

        List<CartItem> cartItems = cartService.findCartItem(loginMember.getId());
        model.addAttribute("cartItems", cartItems);

        return "cart/cartList";
    }

    // 장바구니 아이템 추가
    @PostMapping("/add")
    public String addCart(@RequestParam("itemId") Long itemId,
                          @RequestParam("count") int count,
                          @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                          RedirectAttributes redirectAttributes) {

        if (loginMember == null) {
            return "redirect:/member/login";
        }
        log.info("장바구니 아이템 추가 요청: memberId={}, itemId={}, count={}", loginMember.getId(), itemId, count);
        cartService.addCart(loginMember.getId(), itemId, count);

        return "redirect:/shop/item/" + itemId;
    }

    // 장바구니 아이템 제거
    @PostMapping("/delete")
    public String deleteCart(@RequestParam("itemId") Long itemId,
                             @RequestParam("count") int count,
                             @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                             RedirectAttributes redirectAttributes) {
        log.info("장바구니 아이템 추가 요청: memberId={}, itemId={}, count={}", loginMember.getId(), itemId, count);

        return "redirect:/";
    }
}
