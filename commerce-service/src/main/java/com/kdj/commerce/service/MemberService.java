package com.kdj.commerce.service;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public void join(Member member) {
        memberRepository.findByEmail(member.getEmail())
                .ifPresent(m -> {
                    throw new IllegalStateException("사용중인 이메일입니다.");
                });
        memberRepository.findByLoginId(member.getLoginId())
                .ifPresent(m -> {
                    throw new IllegalStateException("사용중인 아이디입니다.");
                });
        memberRepository.save(member);
    }

    public Member login(String loginId, String loginPassword) {
        return memberRepository.findByLoginId(loginId)
                .filter(m -> m.getLoginPassword().equals(loginPassword))
                .orElse(null);
    }

    public Optional<Member> findMember(long id) {
        return memberRepository.findById(id);
    }

    public List<Member> findMembers() {
        return memberRepository.findAll();
    }
}
