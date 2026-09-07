package com.kdj.commerce.domain.purchase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findAll();

    @Query("""
                select o 
                from Purchase o 
                join fetch o.member
                """)
    List<Purchase> findAllWithMember();

    @Query(
            value = """
                    select o
                    from Purchase o
                    join fetch o.member
                    where o.member.id = :memberId
                    """,
            countQuery = """
                    select count(o)
                    from Purchase o
                    where o.member.id = :memberId
                    """
    )
    Page<Purchase> findByMemberIdWithMember(Pageable pageable, @Param("memberId") Long memberId);

}
