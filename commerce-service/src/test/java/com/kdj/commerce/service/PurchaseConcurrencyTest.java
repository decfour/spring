package com.kdj.commerce.service;

import com.kdj.commerce.domain.item.DeliveryType;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemRepository;
import com.kdj.commerce.domain.item.ItemType;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.domain.purchase.PurchaseItemRepository;
import com.kdj.commerce.domain.purchase.PurchaseRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class PurchaseConcurrencyTest {
    @Autowired PurchaseService purchaseService;

    @Autowired MemberRepository memberRepository;
    @Autowired ItemRepository itemRepository;
    @Autowired PurchaseRepository purchaseRepository;
    @Autowired PurchaseItemRepository purchaseItemRepository;

    private final List<Long> memberIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {

        purchaseItemRepository.deleteAllInBatch();
        purchaseRepository.deleteAllInBatch();
        itemRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        memberIds.clear();
    }

    @Test
    @DisplayName("재고 100개에 대한 동시 주문 100건 처리 후 재고가 0이 된다")
    void concurrencyPurchaseTest() throws Exception {

        // given    : 회원 100명, 재고 100개 이벤트 상품 DB 저장
        for (int i = 1; i <= 100; i++) {
            Member member = Member.create(
                    "user" + i,
                    "testUser" + i + "@test.com",
                    "testId" + i,
                    "testPassword",
                    MemberType.USER
            );
            memberRepository.save(member);
            memberIds.add(member.getId());
        }
        Item item = Item.create(
                "우주 최강 초콜릿",
                1000,
                100,
                "유니콘의 눈물 0.1% 포함",
                true,
                false,
                ItemType.ETC,
                DeliveryType.STANDARD,
                memberIds.get(0),
                null,
                null
        );
        Item savedItem = itemRepository.save(item);
        Long itemId = savedItem.getId();

        var executor = Executors.newFixedThreadPool(32);
        try {
            var tasks = java.util.stream.IntStream.range(0, 100)
                    .mapToObj(i -> executor.submit(() -> purchaseService.purchase(memberIds.get(i), itemId, 1, "name", "address"))).toList();
            for (var task : tasks) task.get(30, java.util.concurrent.TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        // then     : 최종 재고 검증
        Item findItem = itemRepository.findById(itemId).orElseThrow();

        assertThat(findItem.getStock()).isEqualTo(0);
    }

}
