package com.kdj.commerce.service;

import com.kdj.commerce.domain.item.DeliveryType;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemRepository;
import com.kdj.commerce.domain.item.ItemType;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import com.kdj.commerce.domain.order.Order;
import com.kdj.commerce.domain.order.OrderItemRepository;
import com.kdj.commerce.domain.order.OrderRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class OrderServiceTest {
    @Autowired OrderService orderService;

    @Autowired MemberRepository memberRepository;
    @Autowired ItemRepository itemRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderItemRepository orderItemRepository;

    private final List<Long> memberIds = new ArrayList<>();

    @Autowired
    EntityManager em;

    @BeforeEach
    public void setUp () {
        System.out.println("==================== 청소 시작");
        orderItemRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        itemRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        memberIds.clear();
        System.out.println("==================== 청소 종료");
    }

    @Test
    @DisplayName("동시 주문 테스트")
    public void concurrencyOrderTest() throws InterruptedException {
        System.out.println("==================== 동시 주문 테스트 시작");
        // given    : 회원 100명, 재고 100개 이벤트 상품 DB 저장
        for (int i = 1; i <= 100; i++) {
            Member member = Member.createMember(
                    "testUser" + i,
                    "testUser" + i + "@test.com",
                    "testId" + i,
                    "testPassword"
            );
            memberRepository.save(member);
            memberIds.add(member.getId());
        }
        Item item = Item.createItem(
                "우주 최강 초콜릿",
                1000,
                100,
                "유니콘의 눈물 0.1% 포함",
                true,
                ItemType.ETC,
                DeliveryType.STANDARD,
                memberIds.get(0),
                null,
                null
        );
        Item savedItem = itemRepository.save(item);
        Long itemId = savedItem.getId();

        int threadCount = 100;
        ExecutorService executorService =
                Executors.newFixedThreadPool(32);       // 32개 스레드 생성
        CountDownLatch latch =
                new CountDownLatch(threadCount);                // 100개 작업 완료 대기용

        // when     : 100개 스레드가 동시에 각 회원 ID로 주문
        for (int i = 0; i < threadCount; i++) {
            Long memberId = memberIds.get(i);
            executorService.submit(() -> {
                try {
                    // 1개씩 주문 (OrderService 내 findByIdWithLock으로 비관적 락)
                    orderService.order(memberId, itemId, 1);
                } catch (Exception e) {
                    System.out.println("주문 실패 원인: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then     : 최종 재고 검증
        Item findItem = itemRepository.findById(itemId).orElseThrow();
        System.out.println("최종 재고 = " + findItem.getStock());
        assertThat(findItem.getStock()).isEqualTo(0);
        System.out.println("==================== 동시 주문 테스트 종료");
    }

    @Test
    @Transactional
    @DisplayName("N+1 방지 테스트")
    void nPlusOneTest() {
        System.out.println("==================== N+1 방지 테스트 시작");
        // given    : 회원 10명, 각 회원별 상품 1개, 각 회원별 주문 1개 DB 저장
        for (int i = 1; i <= 10; i++) {
            Member member = Member.createMember(
                    "test2User" + i,
                    "test2User" + i + "@test.com",
                    "test2Id" + i,
                    "test2Password"
            );
            memberRepository.save(member);
            Item item = Item.createItem(
                    "item" + i,
                    1000,
                    100,
                    "test",
                    true,
                    ItemType.ETC,
                    DeliveryType.STANDARD,
                    member.getId(),
                    null,
                    null
            );
            itemRepository.save(item);
            orderService.order(member.getId(), item.getId(), 1);
        }

        em.flush();
        em.clear();

        // when
        List<Order> orders = orderService.findAll();
        System.out.println("========== Order 조회 완료");

        // then
        orders.forEach(order ->
                order.getMember().getUsername());
        System.out.println("==================== N+1 방지 테스트 종료");
    }


}