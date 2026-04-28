package com.kdj.commerce.service;

import com.kdj.commerce.domain.Member;
import com.kdj.commerce.repository.MemberRepository;
import com.kdj.commerce.repository.MemoryMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {
    MemberRepository memberRepository;

    @Autowired
    public MemberService(MemoryMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 회원가입
    public void join(Member member) {
        // 이메일 중복
        memberRepository.findByEmail(member.getEmail())
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 사용중인 이메일입니다.");
                });
        memberRepository.save(member);
    }

    // 멤버 조회(개인)
    public Optional<Member> findMember(long id) {

        return memberRepository.findById(id);
    }

    // 멤버 조회(모두)
    public List<Member> findMembers() {

        return memberRepository.findAll();
    }
}
