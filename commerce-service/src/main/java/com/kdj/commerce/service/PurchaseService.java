package com.kdj.commerce.service;

import com.kdj.commerce.domain.cart.CartItem;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemRepository;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import com.kdj.commerce.domain.purchase.Purchase;
import com.kdj.commerce.domain.purchase.PurchaseItem;
import com.kdj.commerce.domain.purchase.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PurchaseService {
    private final CartService cartService;
    private final PurchaseRepository purchaseRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    public Purchase findById(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다"));

        return purchase;
    }

    @Transactional
    public Long purchase(
            Long memberId,
            Long itemId,
            int quantity,
            String receiverName,
            String receiverAddress
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Item item = itemRepository.findByIdWithLock(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        PurchaseItem purchaseItem = PurchaseItem.create(item, item.getPrice(), quantity);
        Purchase purchase = Purchase.create(member, receiverName, receiverAddress, purchaseItem);
        purchaseRepository.save(purchase);

        log.info("주문 생성 purchaseId={}, memberId={}, itemId={}, quantity={}",
                purchase.getId(), memberId, itemId, quantity);

        return purchase.getId();
    }

    @Transactional
    public Long purchaseCart(
            Long memberId,
            List<CartItem> cartItems,
            String receiverName,
            String receiverAddress
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        List<PurchaseItem> purchaseItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Item item = itemRepository.findByIdWithLock(cartItem.getItem().getId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 품절된 상품입니다."));

            PurchaseItem purchaseItem = PurchaseItem.create(
                    item,
                    item.getPrice(),
                    cartItem.getQuantity()
            );

            purchaseItems.add(purchaseItem);
        }

        Purchase purchase = Purchase.create(
                member, receiverName,
                receiverAddress,
                purchaseItems.toArray(new PurchaseItem[0])
        );

        purchaseRepository.save(purchase);
        cartService.clearItem(memberId);

        log.info("주문 생성(장바구니) purchaseId={}, memberId={}, itemCount={}",
                purchase.getId(), memberId, purchaseItems.size());

        return purchase.getId();
    }

    @Transactional
    public void cancel(Long purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        purchase.cancel();

        log.info("주문 취소 purchaseId={}", purchaseId);
    }

    public int getTotalPrice(Long id) {
        Purchase purchase = purchaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        return purchase.getTotalPrice();
    }

    public List<Purchase> findAll() {
        return purchaseRepository.findAll();}

    // Fetch Join으로 회원 정보를 함께 조회하여 N+1 방지
    public Page<Purchase> findByMemberId(Pageable pageable, Long id) {
        return purchaseRepository.findByMemberIdWithMember(pageable, id);
    }

    public List<Purchase> findAllFetch() {
        return purchaseRepository.findAllWithMember();
    }
}
