package com.kdj.commerce.domain.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("select o from Order o join fetch o.member")
    List<Order> findAllWithMember();

    @Query("select o from Order o join fetch o.member where o.member.id = :memberId")
    List<Order> findByMemberIdWithMember(@Param("memberId") Long memberId);

}
