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
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        // 동시 주문 시 재고 정합성을 위해 비관적 락
        Item item = itemRepository.findByIdWithLock(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);
        Order order = Order.createOrder(member, orderItem);
        orderRepository.save(order);

        log.info("주문 생성 orderId={}, memberId={}, itemId={}, quantity={}",
                order.getId(), memberId, itemId, count);

        return order.getId();
    }

    @Transactional
    public Long orderCart(Long memberId, List<CartItem> cartItems) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            // 동시 주문 시 재고 정합성을 위해 비관적 락
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

        log.info("주문 생성(장바구니) orderId={}, memberId={}, itemCount={}",
                order.getId(), memberId, orderItems.size());

        return order.getId();
    }

    @Transactional
    public void cancel(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        order.cancel();

        log.info("주문 취소 orderId={}, memberId={}", orderId, memberId);
    }

    public int getTotalPrice(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        return order.getTotalPrice();
    }

    public List<Order> findAll() {return orderRepository.findAll();}

    // Fetch Join으로 회원 정보를 함께 조회하여 N+1 방지
    public List<Order> findByMemberId(Long id) {
        return orderRepository.findByMemberIdWithMember(id);
    }

    public List<Order> findAllFetch() {
        return orderRepository.findAllWithMember();
    }
}
