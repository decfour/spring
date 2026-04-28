package com.kdj.commerce.repository;

import com.kdj.commerce.domain.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<Member> findByUsername(String username);
    Optional<Member> findByEmail(String email);
    List<Member> findAll();
}
