package com.kdj.commerce.domain.cart;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 쿼리 메서드
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByMemberId(Long memberId);
}
