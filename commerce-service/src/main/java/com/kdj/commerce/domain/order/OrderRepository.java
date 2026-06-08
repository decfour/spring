package com.kdj.commerce.domain.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByMemberId(Long memberId);

    // fetch join (Order 조회 시 연관된 Member까지 INNER JOIN으로 한 방에 데이터를 채운다)
    @Query("select o from Order o join fetch o.member")
    List<Order> findAllWithMember();

}
