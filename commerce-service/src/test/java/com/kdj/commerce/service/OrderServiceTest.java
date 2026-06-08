package com.kdj.commerce.service;

import com.kdj.commerce.domain.cart.CartItemRepository;
import com.kdj.commerce.domain.cart.CartRepository;
import com.kdj.commerce.domain.item.DeliveryType;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemRepository;
import com.kdj.commerce.domain.item.ItemType;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import com.kdj.commerce.domain.order.Order;
import com.kdj.commerce.domain.order.OrderItem;
import com.kdj.commerce.domain.order.OrderItemRepository;
import com.kdj.commerce.domain.order.OrderRepository;
import com.kdj.commerce.exception.NotEnoughStockException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class OrderServiceTest {

    @Autowired private OrderService orderService;
    @Autowired private ItemRepository itemRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private OrderItemRepository orderItemRepository;

    @PersistenceContext
    private EntityManager em;

    @AfterEach
    void cleanUp() {
        System.out.println("=== 청소 시작 ===");
        cartItemRepository.deleteAllInBatch();
        orderItemRepository.deleteAllInBatch();
        cartRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        itemRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        System.out.println("=== 청소 종료 ===");
    }

    // 1. 멀티스레드 동시성 테스트 - 비관적 락 정상 작동, 레이스 컨디션이 발생하지 않는지 검증
    @Test
    @DisplayName("100명이 동시에 각각 1개씩 주문 시 재고가 0개")
    void order_ConcurrencyRequest_DecreaseStockExactly() throws InterruptedException {
        // Given 테스트 환경 구축 및 데이터 준비
        Member member = Member.createMember("testUser", "test@example.com", "testId", "testPw");
        memberRepository.save(member);

        Item item = new Item();
        item.setName("선착순 특가 상품");
        item.setPrice(1000);
        item.setStock(100);
        item.setItemType(ItemType.BOOK);
        item.setCreatedBy(member.getId());
        item.setDeliveryType(DeliveryType.STANDARD);
        itemRepository.save(item);

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When 100개의 스레드가 동시에 주문 비즈니스 로직을 호출
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    orderService.order(member.getId(), item.getId(), 1);
                } catch (NotEnoughStockException e) {
                    System.out.println("재고 부족 예외 처리: " + e.getMessage());
                } finally {
                    latch.countDown(); // 스레드 작업 완료 알림
                }
            });
        }
        latch.await(); // 모든 스레드가 종료될 때까지 메인 스레드 대기

        // Then 재고가 정상적으로 차감되어 0개가 되었는지 확인
        Item findItem = itemRepository.findById(item.getId()).orElseThrow();
        System.out.println("=== 최종 남은 재고량: " + findItem.getStock() + "개 ===");

        assertThat(findItem.getStock()).isEqualTo(0);
    }

    // 2. JPA 기본 지연 로딩, N+1 문제 재현 테스트
    @Test
    @Transactional
    @DisplayName("일반 findAll 조회 후 연관 엔티티 접근 시 N+1 문제 발생")
    void findAll_LazyLoadingDefault_OccurNPlusOneProblem() {
        // Given 영속성 컨텍스트 및 DB에 주문 데이터가 저장되어 있는 상태 가정 (3개 이상)
        Member m1 = Member.createMember("유저1", "user1@test.com", "id1", "pw1");
        Member m2 = Member.createMember("유저2", "user2@test.com", "id2", "pw2");
        Member m3 = Member.createMember("유저3", "user3@test.com", "id3", "pw3");
        memberRepository.save(m1); memberRepository.save(m2); memberRepository.save(m3);

        Order o1 = Order.createOrder(m1);
        Order o2 = Order.createOrder(m2);
        Order o3 = Order.createOrder(m3);
        orderRepository.save(o1); orderRepository.save(o2); orderRepository.save(o3);

        em.flush();
        em.clear();

        // When 일반 findAll 쿼리로 주문 목록만 먼저 조회 (1번의 쿼리)
        System.out.println("=== 1. 주문 전체 조회 시작 (1번의 쿼리) ===");
        List<Order> orders = orderRepository.findAll();

        // Then 루프를 돌며 가짜 객체(Proxy) Member에 접근할 때마다 추가 SELECT 쿼리 (N번 실행)
        System.out.println("=== 2. 지연 로딩 객체 강제 초기화 시작 (N번의 추가 쿼리) ===");
        for (Order order : orders) {
            System.out.println("주문한 회원 이름: " + order.getMember().getUsername());
        }
    }

    // 3. 페치 조인(Fetch Join) 이용, N+1 해결 최적화 테스트
    @Test
    @Transactional
    @DisplayName("Fetch Join 사용 시 연관 엔티티까지 단 1번의 JOIN 쿼리로 가져옴")
    void findAllWithMember_FetchJoin_SuccessWithSingleQuery() {
        // Given 영속성 컨텍스트 및 DB에 주문 데이터가 저장되어 있는 상태 가정 (3개 이상)
        Member m1 = Member.createMember("유저1", "user1@test.com", "id1", "pw1");
        Member m2 = Member.createMember("유저2", "user2@test.com", "id2", "pw2");
        Member m3 = Member.createMember("유저3", "user3@test.com", "id3", "pw3");
        memberRepository.save(m1); memberRepository.save(m2); memberRepository.save(m3);

        Order o1 = Order.createOrder(m1); Order o2 = Order.createOrder(m2); Order o3 = Order.createOrder(m3);
        orderRepository.save(o1); orderRepository.save(o2); orderRepository.save(o3);

        em.flush();
        em.clear();

        // When join fetch 문법이 적용된 커스텀 리포지토리 메서드 호출
        System.out.println("=== 1. 페치 조인 전체 조회 시작 (1번의 쿼리) ===");
        List<Order> orders = orderRepository.findAllWithMember();

        // Then 이미 진짜 Member 객체가 로딩되어 필드 접근 시 추가 쿼리가 안 터짐
        System.out.println("=== 2. 지연 로딩 객체 접근 (추가 쿼리가 안 터져야 성공) ===");
        for (Order order : orders) {
            System.out.println("주문한 회원 이름: " + order.getMember().getUsername());
        }
    }

    // 4. @OneToMany 컬렉션 조회 및 배치 사이즈 최적화 테스트
    @Test
    @Transactional
    @DisplayName("배치 사이즈 설정을 켜면 OneToMany 자식 컬렉션들이 IN 절 한 방으로 묶여서 조회")
    void orderItems_LazyLoadingWithBatchSize_OptimizeWithInClause() {
        // Given 주문 데이터 및 자식 OrderItem 데이터들이 준비 완료된 상태 가정
        Member m1 = Member.createMember("유저1", "user1@test.com", "id1", "pw1");
        memberRepository.save(m1);

        // 상품 정보 생성 (Validation 통과용 필수 필드들 기입)
        Item item = new Item();
        item.setName("선착순 특가 상품"); item.setPrice(1000); item.setStock(100); item.setItemType(ItemType.BOOK);
        item.setCreatedBy(1L); item.setDeliveryType(DeliveryType.STANDARD); // 아까 터진 Validation 방지
        itemRepository.save(item);

        // 주문 3개 생성
        Order o1 = Order.createOrder(m1); Order o2 = Order.createOrder(m1); Order o3 = Order.createOrder(m1);

        OrderItem oi1 = OrderItem.createOrderItem(item, 1000, 1); o1.addOrderItem(oi1);
        OrderItem oi2 = OrderItem.createOrderItem(item, 1000, 1); o2.addOrderItem(oi2);
        OrderItem oi3 = OrderItem.createOrderItem(item, 1000, 2); o3.addOrderItem(oi3);

        orderRepository.save(o1); orderRepository.save(o2); orderRepository.save(o3);

        em.flush();
        em.clear();

        List<Order> orders = orderRepository.findAllWithMember();

        // When & Then 루프를 돌며 자식 컬렉션(orderItems)의 사이즈에 접근
        System.out.println("=== [배치 사이즈 작동] 자식 컬렉션 강제 접근 시작 ===");
        for (Order order : orders) {
            // default_batch_fetch_size 설정 덕분에 각각 루프마다 쿼리가 안 터지고,
            // 현재 영속성 컨텍스트에 있는 Order ID들을 묶어 IN 절 딱 1번으로 조회해옴
            int totalSize = order.getOrderItems().size();
            System.out.println("주문 상품 개수: " + totalSize);
        }
    }
}