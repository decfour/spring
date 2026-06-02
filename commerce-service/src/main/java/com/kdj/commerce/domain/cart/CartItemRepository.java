package com.kdj.commerce.domain.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndItemId(Long cartId, Long itemId);

    @Query("select ci from CartItem ci join fetch ci.item where ci.cart.id = :cartId")
    List<CartItem> findAllByCartId(@Param("cartId") Long cartId);

    void deleteByCartId(Long cartId);
}
