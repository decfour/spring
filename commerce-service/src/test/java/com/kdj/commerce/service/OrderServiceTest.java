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
import org.springframework.transaction.annotation.Transactional;
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

// 스프링 부트 컨테이너 통합 테스트
@SpringBootTest
class OrderServiceTest {
    @Autowired private OrderService orderService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ItemRepository itemRepository;

    // JPA 영속성 컨텍스트 직접 제어
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

    // 1. 멀티스레드 동시성 테스트 - 비관적 Lock
    @Test
    @DisplayName("100명이 동시에 1개씩 주문 시")
    void order_ConcurrencyRequest_DecreaseStockExactly() throws InterruptedException {
        // Given: 회원 1명 생성, 100개 재고 상품 1개 생성
        System.out.println("=== Given ===");
        Member member = Member.createMember("testUser", "test@example.com", "testId", "testPw");
        memberRepository.save(member);

        Item item = new Item();
        item.setName("상품");
        item.setPrice(1000);
        item.setStock(100);
        item.setItemType(ItemType.BOOK);
        item.setCreatedBy(member.getId());
        item.setDeliveryType(DeliveryType.STANDARD);
        itemRepository.save(item);

        // 멀티스레드 환경 세팅
        int threadCount = 100;
        // 32개 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        // 100개 스레드가 모두 끝날 때까지 메인 스레드를 붙잡는 카운트다운
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When: 100개의 스레드가 비동기로 동시에 orderService.order() 호출
        System.out.println("=== When: 100개 주문 발생 ===");
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    orderService.order(member.getId(), item.getId(), 1);
                } catch (NotEnoughStockException e) {
                    System.out.println("재고 부족 예외 처리: " + e.getMessage());
                } finally {
                    latch.countDown(); // 작업이 끝난 스레드는 카운트를 1씩 감소 (0이 되면 대기 해제)
                }
            });
        }

        // 모든 스레드가 작업을 완료(카운트 0)할 때까지 메인 스레드는 대기
        latch.await();

        // Then: DB에서 상품을 꺼내와 레이스 컨디션 없이 100개가 정확히 차감되어 0개인지 확인
        Item findItem = itemRepository.findById(item.getId()).orElseThrow();
        System.out.println("=== Then: 최종 재고 -> " + findItem.getStock() + " ===");

        assertThat(findItem.getStock()).isEqualTo(0);
    }

    // 2. JPA 지연 로딩과 N+1 문제 재현 테스트
    @Test
    @Transactional
    @DisplayName("findAll 조회 후 연관 엔티티 접근 시 N+1 문제 (ManyToOne)")
    void findAll_LazyLoadingDefault_OccurNPlusOneProblem() {
        // Given: 회원 3명과 각각의 주문 3개를 생성 후 DB에 저장
        Member m1 = Member.createMember("유저1", "user1@test.com", "id1", "pw1");
        Member m2 = Member.createMember("유저2", "user2@test.com", "id2", "pw2");
        Member m3 = Member.createMember("유저3", "user3@test.com", "id3", "pw3");
        memberRepository.save(m1); memberRepository.save(m2); memberRepository.save(m3);

        Order o1 = Order.createOrder(m1);
        Order o2 = Order.createOrder(m2);
        Order o3 = Order.createOrder(m3);
        orderRepository.save(o1); orderRepository.save(o2); orderRepository.save(o3);

        // 영속성 컨텍스트를 DB와 동기화 후 비우기
        em.flush();
        em.clear();

        // When: 주문 전체 조회 쿼리 (Order만 가져오는 1번의 쿼리 발생)
        System.out.println("=== 1. 주문 전체 조회 시작 (1번의 쿼리) ===");
        List<Order> orders = orderRepository.findAll();

        // Then: Order 내부의 가짜 객체(Proxy 상태 Member)의 실체 데이터에 접근
        System.out.println("=== 2. 지연 로딩 객체 강제 초기화 시작 ===");
        for (Order order : orders) {
            // .getUsername()을 호출 시 프록시 객체가 아닌 DB에 회원 조회 쿼리를 각각 날림 (N번의 추가 쿼리)
            System.out.println("주문한 회원 이름: " + order.getMember().getUsername());
        }
    }

    // 3. Fetch Join 최적화 테스트 - 조인 쿼리 하나로 Order에 Member까지 가져와서 루프 시 추가 쿼리 없음
    @Test
    @Transactional
    @DisplayName("Fetch Join 사용 시 연관 엔티티까지 단 한 번의 JOIN 쿼리로 가져옴")
    void findAllWithMember_FetchJoin_SuccessWithSingleQuery() {
        // Given: 데이터 세팅 후 영속성 컨텍스트 청소
        Member m1 = Member.createMember("유저1", "user1@test.com", "id1", "pw1");
        Member m2 = Member.createMember("유저2", "user2@test.com", "id2", "pw2");
        Member m3 = Member.createMember("유저3", "user3@test.com", "id3", "pw3");
        memberRepository.save(m1); memberRepository.save(m2); memberRepository.save(m3);

        Order o1 = Order.createOrder(m1); Order o2 = Order.createOrder(m2); Order o3 = Order.createOrder(m3);
        orderRepository.save(o1); orderRepository.save(o2); orderRepository.save(o3);

        em.flush();
        em.clear();

        // When: 일반 findAll()이 아니라 JPQL로 `select o from Order o join fetch o.member`커스텀 메서드 호출
        System.out.println("=== 1. 페치 조인 전체 조회 시작 (1번의 쿼리) ===");
        // 내부 Member 객체들이 Proxy가 아닌 실제 데이터
        List<Order> orders = orderRepository.findAllWithMember(); // 내부 Member 객체들이 진짜 데이터로 채워짐

        // Then: 단일 쿼리에서 회원 데이터까지 통째로 다 들고 왔으므로, 루프 내부에서 추가 쿼리가 발생 안 함
        System.out.println("=== 2. 지연 로딩 객체 접근 (추가 쿼리가 안 터져야 성공) ===");
        for (Order order : orders) {
            System.out.println("주문한 회원 이름: " + order.getMember().getUsername());
        }
    }

    @Test
    @Transactional
    @DisplayName("유저 1명의 주문 1개에 상품이 여러 개 담겼을 때의 데이터뻥튀기")
    void orderItems_OneOrderWithMultipleItems_Optimize() {
        // Given: 유저 1명, 상품 3개 생성
        Member m1 = Member.createMember("유저1", "user1@test.com", "id1", "pw1");
        memberRepository.save(m1);

        Item item1 = new Item();
        item1.setName("상품1");
        item1.setPrice(1000);
        item1.setStock(100);
        item1.setItemType(ItemType.BOOK);
        item1.setCreatedBy(m1.getId());
        item1.setDeliveryType(DeliveryType.STANDARD);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("상품2");
        item2.setPrice(1000);
        item2.setStock(100);
        item2.setItemType(ItemType.BOOK);
        item2.setCreatedBy(m1.getId());
        item2.setDeliveryType(DeliveryType.STANDARD);
        itemRepository.save(item2);

        Item item3 = new Item();
        item3.setName("상품3");
        item3.setPrice(1000);
        item3.setStock(100);
        item3.setItemType(ItemType.BOOK);
        item3.setCreatedBy(m1.getId());
        item3.setDeliveryType(DeliveryType.STANDARD);
        itemRepository.save(item3);

        Order o1 = Order.createOrder(m1);

        OrderItem oi1 = OrderItem.createOrderItem(item1, 1000, 1);
        o1.addOrderItem(oi1);
        OrderItem oi2 = OrderItem.createOrderItem(item2, 1000, 1);
        o1.addOrderItem(oi2);
        OrderItem oi3 = OrderItem.createOrderItem(item3, 1000, 1);
        o1.addOrderItem(oi3);

        orderRepository.save(o1);

        em.flush();
        em.clear();

        // When: 주문 조회
        System.out.println("=== 1. 주문 단건 조회 ===");
        Order findOrder = orderRepository.findById(o1.getId()).orElseThrow();

        // Then: 자식 컬렉션에 접근하여 개수 확인
        System.out.println("=== 2. 자식 컬렉션(OrderItem들) 접근 ===");
        int totalSize = findOrder.getOrderItems().size();
        System.out.println("이 주문에 담긴 상품 종류 개수: " + totalSize);
    }
}