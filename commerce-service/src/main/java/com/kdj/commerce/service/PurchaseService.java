package com.kdj.commerce.service;

import com.kdj.commerce.domain.cart.CartItem;
import com.kdj.commerce.domain.item.Item;
import com.kdj.commerce.domain.item.ItemRepository;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import com.kdj.commerce.domain.purchase.Purchase;
import com.kdj.commerce.domain.purchase.PurchaseItem;
import com.kdj.commerce.domain.purchase.PurchaseRepository;
import com.kdj.commerce.exception.PermissionDeniedException;
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

    // Fetch Join으로 회원 정보를 함께 조회하여 N+1 방지
    public Page<Purchase> findByMemberId(Pageable pageable, Long id) {
        return purchaseRepository.findByMemberIdWithMember(pageable, id);
    }

    public List<Purchase> findAll() {
        return purchaseRepository.findAll();
    }

    @Transactional
    public Long purchase(
            Long memberId,
            Long itemId,
            Integer quantity,
            String receiverName,
            String receiverAddress
    ) {
        Member member = findOrderingMember(memberId);
        validateQuantity(quantity);
        Item item = findPurchasableItem(itemId);

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
            String receiverName,
            String receiverAddress
    ) {
        Member member = findOrderingMember(memberId);
        List<CartItem> cartItems = cartService.findItem(memberId);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어 있습니다.");
        }

        List<PurchaseItem> purchaseItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            validateQuantity(cartItem.getQuantity());
            Item item = findPurchasableItem(cartItem.getItem().getId());

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
    public void cancel(Long purchaseId, Long memberId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        isOwner(purchase, memberId);
        purchase.cancel();

        log.info("주문 취소 purchaseId={}", purchaseId);
    }


    private Member findOrderingMember(Long memberId) {
        if (memberId == null) {
            throw new PermissionDeniedException("로그인이 필요합니다.");
        }

        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("주문 수량은 1개 이상이어야 합니다.");
        }
    }

    private Item findPurchasableItem(Long itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("주문할 상품을 선택해 주세요.");
        }
        Item item = itemRepository.findByIdWithLock(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        if (item.isDeleted() || !item.isOpen()) {
            throw new IllegalStateException("판매 중인 상품만 주문할 수 있습니다.");
        }

        return item;
    }

    private void isOwner(Purchase purchase, Long memberId) {
        if (memberId == null || !memberId.equals(purchase.getMember().getId())) {
            throw new PermissionDeniedException("본인의 주문만 취소할 수 있습니다.");
        }
    }
}
