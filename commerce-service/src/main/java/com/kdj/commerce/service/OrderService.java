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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public Long order(Long memberId, Long itemId, int count) {

        // 1. 회원, 상품 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        // 2. 주문 상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        // 3. 주문 생성
        Order order = Order.createOrder(member, orderItem);

        // 4. 주문 저장
        orderRepository.save(order);

        return order.getId();
    }

    // 주문 취소
    @Transactional
    public void cancelOrder(Long orderId) {
        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        // 주문 취소
        order.cancel();
    }

    public List<Order> findOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Long cartOrder(Long memberId, List<CartItem> cartItems) {

        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));


        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = OrderItem.createOrderItem(
                    cartItem.getItem(),
                    cartItem.getItem().getPrice(),
                    cartItem.getCount()
            );
            orderItems.add(orderItem);
        }

        Order order = Order.createOrder(member, orderItems.toArray(new OrderItem[0]));
        orderRepository.save(order);

        return order.getId();
    }
}
