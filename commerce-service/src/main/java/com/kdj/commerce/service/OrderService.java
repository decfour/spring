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

    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        log.info("START OrderService/order");

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Item item = itemRepository.findByIdWithLock(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);
        Order order = Order.createOrder(member, orderItem);
        orderRepository.save(order);

        log.info("END   OrderService/order");

        return order.getId();
    }

    @Transactional
    public Long orderCart(Long memberId, List<CartItem> cartItems) {
        log.info("START OrderService/cartOrder");

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Item lockItem = itemRepository.findByIdWithLock(cartItem.getItem().getId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 품절된 상품입니다."));
            OrderItem orderItem = OrderItem.createOrderItem(
                    lockItem,
                    lockItem.getPrice(),
                    cartItem.getCount()
            );
            orderItems.add(orderItem);
        }

        Order order = Order.createOrder(member, orderItems.toArray(new OrderItem[0]));
        orderRepository.save(order);
        cartService.clearCart(memberId);

        log.info("END   OrderService/cartOrder");
        return order.getId();
    }

    @Transactional
    public void cancel(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        order.cancel();
    }

    public int getTotalPrice(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        return order.getTotalPrice();
    }

    // 사용자 : 개인 주문 내역 조회 (Fetch)
    public List<Order> findByMemberId(Long id) {
        return orderRepository.findByMemberIdWithMember(id);
    }

    // 관리자 : 전체 주문 내역 조회 (Fetch)
    public List<Order> findAll() {
        return orderRepository.findAllWithMember();
    }
}
