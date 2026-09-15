package com.kdj.commerce.domain.purchase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
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
