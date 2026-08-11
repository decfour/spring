package com.kdj.commerce.service;

import com.kdj.commerce.domain.cart.Cart;
import com.kdj.commerce.domain.cart.CartItem;
import com.kdj.commerce.domain.cart.CartItemRepository;
import com.kdj.commerce.domain.cart.CartRepository;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemRepository;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import com.kdj.commerce.exception.NotEnoughStockException;
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
    public void add(Long memberId, Long itemId, int count) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다 id=" + memberId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다 id=" + itemId));
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseGet(() -> cartRepository.save(Cart.create(member)));

        Optional<CartItem> existingItem =
                cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());

        int requestedCount = existingItem
                .map(cartItem -> cartItem.getCount() + count)
                .orElse(count);

        if (requestedCount > item.getStock()) {
            throw new NotEnoughStockException("재고가 부족합니다. (재고: " + item.getStock() + "개)");
        }

        if (existingItem.isPresent()) {
            existingItem.get().addCount(count);
        } else {
            cartItemRepository.save(CartItem.create(cart, item, count));
        }
    }

    @Transactional
    public void clear(Long memberId) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 없습니다."));

        cartItemRepository.deleteByCartId(cart.getId());
    }

    public List<CartItem> findCartItem(Long memberId) {
        return cartRepository.findByMemberId(memberId)
                .map(cart -> cartItemRepository.findAllByCartId(cart.getId()))
                .orElse(Collections.emptyList());
    }

    @Transactional
    public void deleteCartItem(Long memberId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findByIdAndMemberId(cartItemId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));

        cartItemRepository.delete(cartItem);
    }
}
