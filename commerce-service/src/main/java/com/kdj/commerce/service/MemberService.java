package com.kdj.commerce.service;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public Optional<Member> findById(long id) {
        return memberRepository.findById(id);
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 회원이 존재하지 않습니다."));
    }

    @Transactional
    public void signUp(Member member) {
        memberRepository.findByEmail(member.getEmail())
                .ifPresent(m -> {
                    throw new IllegalStateException("사용중인 이메일입니다.");
                });
        memberRepository.findBySignInId(member.getSignInId())
                .ifPresent(m -> {
                    throw new IllegalStateException("사용중인 아이디입니다.");
                });
        memberRepository.save(member);
    }

    public Member signIn(String signInId, String signInPassword) {
        return memberRepository.findBySignInId(signInId)
                .filter(m -> m.getSignInPassword().equals(signInPassword))
                .orElse(null);
    }
}
