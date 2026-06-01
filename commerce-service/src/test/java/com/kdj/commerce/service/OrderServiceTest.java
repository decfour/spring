package com.kdj.commerce.service;

import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemRepository;
import com.kdj.commerce.domain.item.ItemType;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import com.kdj.commerce.domain.order.OrderRepository;
import com.kdj.commerce.exception.NotEnoughStockException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderServiceTest {

    @Autowired private OrderService orderService;
    @Autowired private ItemRepository itemRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private OrderRepository orderRepository;

    @PersistenceContext
    private EntityManager em;

    @AfterEach
    void cleanUp() {
        orderRepository.deleteAll();
        itemRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("100명이 동시에 각각 1개씩 주문 -> 재고가 정확히 0개")
    void concurrencyTest() throws InterruptedException {

        // Given
        Member member = Member.createMember("testUser", "test@example.com", "testId", "testPw");
        memberRepository.save(member);

        Item item = new Item(); //
        item.setName("선착순 특가 상품");
        item.setPrice(1000);
        item.setStock(100);
        item.setItemType(ItemType.BOOK);
        itemRepository.save(item);

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When : 100명이 동시에 주문 요청
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    orderService.order(member.getId(), item.getId(), 1);
                } catch (NotEnoughStockException e) {
                    System.out.println("재고 부족 : " + e.getMessage());
                } finally {
                    latch.countDown(); // 하나의 스레드가 끝날 때마다 숫자를 줄임
                }
            });
        }

        latch.await(); // 100개의 스레드가 전부 일을 마칠 때까지 메인 스레드는 대기

        // Then: 재고가 0?
        Item findItem = itemRepository.findById(item.getId()).orElseThrow();

        System.out.println("=== 최종 남은 재고량: " + findItem.getStock() + "개 ===");

        // 데이터 정합성이 지켜졌다면 100 - 100 = 0
        assertThat(findItem.getStock()).isEqualTo(0);
    }
}