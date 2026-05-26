package com.kdj.commerce.service;

import com.kdj.commerce.domain.cart.Cart;
import com.kdj.commerce.domain.cart.CartItem;
import com.kdj.commerce.domain.cart.CartItemRepository;
import com.kdj.commerce.domain.cart.CartRepository;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemRepository;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public Long addCart(Long memberId, Long itemId, int count) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다. id=" + memberId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다. id=" + itemId));

        Cart cart = cartRepository.findByMemberId(memberId).orElse(null);

        if (cart == null) {
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }

        Optional<CartItem> findCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());

        if (findCartItem.isPresent()) {
            findCartItem.get().addCount(count);
            return findCartItem.get().getId();
        } else {
            CartItem cartItem = CartItem.createCartItem(cart, item, count);
            cartItemRepository.save(cartItem);
            return cartItem.getId();
        }
    }

    public List<CartItem> findCartItem(Long memberId) {
        Optional<Cart> cart = cartRepository.findByMemberId(memberId);

        if (cart.isEmpty()) {
            return Collections.emptyList();
        }

        return cartItemRepository.findAllByCartId(cart.get().getId());
    }
}
