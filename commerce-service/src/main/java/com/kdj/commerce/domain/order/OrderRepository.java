package com.kdj.commerce.domain.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 1. 관리자 페이지 전체 페치 조인
    @Query("select o from Order o join fetch o.member")
    List<Order> findAllWithMember();

    // 2. 마이 페이지 특정 회원 전용 페치 조인
    @Query("select o from Order o join fetch o.member where o.member.id = :memberId")
    List<Order> findByMemberIdWithMember(@Param("memberId") Long memberId);

}
