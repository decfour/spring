package com.kdj.commerce.domain.order;

import com.kdj.commerce.domain.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    // 주문 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    // 주문 회원(회원 1 : N 주문)
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩
    @JoinColumn(name = "member_id")   // order.member_id <-> member.id 조인
    private Member member;

    // 주문 시간
    private LocalDateTime orderDate;

    // 주문 상태
    @Enumerated(EnumType.STRING)
    private OrderStatus status; // 주문 상태 [ORDER, CANCEL]

    // 주문 상품 목록
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) // OrderItem 클래스에 있는 "order" 변수에 의해 매핑
    private List<OrderItem> orderItems = new ArrayList<>();



    // 주문 상품 목록 추가
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    // 주문 생성
    public static Order createOrder(Member member, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    // 주문 취소
    public void cancel() {
        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    // 영수증
    public int getTotalPrice() {
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }
}
