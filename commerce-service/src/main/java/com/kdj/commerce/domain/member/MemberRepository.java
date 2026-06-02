package com.kdj.commerce.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 인터페이스 기본 제공 메서드
    // Member save(Member member);
    // Optional<Member> findById(Long id);
    // List<Member> findAll();

    // 커스텀 메서드
    Optional<Member> findByUsername(String username);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByLoginId(String loginId);
}
