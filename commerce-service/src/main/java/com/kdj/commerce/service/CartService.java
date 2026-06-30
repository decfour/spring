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
    public Long addCart(Long memberId, Long itemId, int count) {
        // 1. 회원, 아이템 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다 id=" + memberId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다 id=" + itemId));

        // 2. 카트 조회, 없으면 생성
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseGet(() -> cartRepository.save(Cart.createCart(member)));

        // 3. 카트 내 아이템 조회, 있으면 담기
        Optional<CartItem> findCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());

        if (findCartItem.isPresent()) {
            int totalCount = findCartItem.get().getCount() + count;
            if (totalCount > item.getStock()) {
                throw new NotEnoughStockException("재고가 부족합니다 (재고: " + item.getStock() + "개)");
            }
            findCartItem.get().addCount(count);

            return findCartItem.get().getId();
        }
        else {
            if (count > item.getStock()) {
                throw new NotEnoughStockException("재고가 부족합니다 (재고: " + item.getStock() + "개)");
            }
            CartItem cartItem = CartItem.createCartItem(cart, item, count);
            cartItemRepository.save(cartItem);

            return cartItem.getId();
        }
    }

    public List<CartItem> findCartItem(Long memberId) {
        return cartRepository.findByMemberId(memberId)
                .map(cart -> cartItemRepository.findAllByCartId(cart.getId()))
                .orElse(Collections.emptyList());
    }

    @Transactional
    public void deleteCartItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional
    public void clearCart(Long memberId) {
        Optional<Cart> cart = cartRepository.findByMemberId(memberId);

        if (cart.isPresent()) {
            List<CartItem> cartItems = cartItemRepository.findAllByCartId(cart.get().getId());
            cartItemRepository.deleteAllInBatch(cartItems);
        }
    }
}
