package com.kdj.commerce.service;

import com.kdj.commerce.domain.cart.CartItem;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemRepository;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import com.kdj.commerce.domain.order.Order;
import com.kdj.commerce.domain.order.OrderItem;
import com.kdj.commerce.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final CartService cartService;

    public int getTotalPrice(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        return order.getTotalPrice();
    }

    // 단건 상품 주문
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {

        log.info("=== 단건 상품 주문 사작 ===");

        // 1. 회원, 상품 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Item item = itemRepository.findByIdWithLock(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        // 2. 주문 상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        // 3. 주문 생성
        Order order = Order.createOrder(member, orderItem);

        // 4. 주문 저장
        orderRepository.save(order);

        log.info("=== 단건 상품 주문 사작 ===");

        // 5. 주문 ID 반환
        return order.getId();
    }

    // 카트 상품 주문
    @Transactional
    public Long cartOrder(Long memberId, List<CartItem> cartItems) {

        log.info("=== 카트 상품 주문 사작 ===");

        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));


        // 2. 주문 상품 목록 생성
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {

            Item lockItem = itemRepository.findByIdWithLock(cartItem.getItem().getId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 품절된 상품입니다."));

            OrderItem orderItem = OrderItem.createOrderItem(
                    cartItem.getItem(),
                    cartItem.getItem().getPrice(),
                    cartItem.getCount()
            );
            orderItems.add(orderItem);
        }

        // 3. 주문 생성
        Order order = Order.createOrder(member, orderItems.toArray(new OrderItem[0]));
        orderRepository.save(order);

        // 4. 카트 비우기 (내부 구현으로 데이터 불일치 방지)
        cartService.clearCart(memberId);

        log.info("=== 카트 상품 주문 종료 ===");

        // 5. 주문 ID 반환
        return order.getId();
    }

    // 주문 취소
    @Transactional
    public void cancelOrder(Long orderId) {

        log.info("=== 주문 종료 ===");

        // 1. 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        // 2. 주문 취소
        order.cancel();
    }

    // 사용자 : 개인 주문 내역 조회 (Fetch)
    public List<Order> findOrdersByMember(Long memberId) {
        return orderRepository.findByMemberIdWithMember(memberId);
    }

    // 관리자 : 전체 주문 내역 조회 (Fetch)
    public List<Order> findAllOrdersForAdmin() {
        return orderRepository.findAllWithMember();
    }

}
