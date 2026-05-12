package com.kdj.commerce.service;

import com.kdj.commerce.domain.member.Member;
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

    public Member login(String loginId, String loginPassword) {
        return memberRepository.findByLoginId(loginId)
                .filter(m -> m.getLoginPassword().equals(loginPassword)) // 2. 찾은 회원의 비밀번호가 입력한 것과 같은지 비교
                .orElse(null); // 3. 없거나 틀리면 null 반환
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
