package com.kdj.commerce.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAll();

    @Query("""
                select o 
                from Order o 
                join fetch o.member
                """)
    List<Order> findAllWithMember();

    @Query(
            value = """
                    select o
                    from Order o
                    join fetch o.member
                    where o.member.id = :memberId
                    """,
            countQuery = """
                    select count(o)
                    from Order o
                    where o.member.id = :memberId
                    """
    )
    Page<Order> findByMemberIdWithMember(Pageable pageable, @Param("memberId") Long memberId);

}
