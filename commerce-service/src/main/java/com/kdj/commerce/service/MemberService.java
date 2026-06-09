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

    // 회원가입
    public void join(Member member) {
        // 이메일 중복 검증
        memberRepository.findByEmail(member.getEmail())
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 사용중인 이메일입니다.");
                });

        // 아이디 중복 검증
        memberRepository.findByLoginId(member.getLoginId())
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 사용중인 아이디입니다.");
                });
        memberRepository.save(member);
    }

    // 로그인
    public Member login(String loginId, String loginPassword) {
        return memberRepository.findByLoginId(loginId)
                .filter(m -> m.getLoginPassword().equals(loginPassword))
                .orElse(null);
    }

    // 개인 회원 조회
    public Optional<Member> findMember(long id) {

        return memberRepository.findById(id);
    }

    // 전체 회원 조회
    public List<Member> findMembers() {

        return memberRepository.findAll();
    }
}
