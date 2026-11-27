package com.kdj.food.service;

import com.kdj.food.entity.Member;
import com.kdj.food.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository members;

    public MemberService(MemberRepository members) {
        this.members = members;
    }

    public Profile getProfile(Long memberId) {
        return profile(findMember(memberId));
    }

    @Transactional
    public Profile rename(Long memberId, String name) {
        if (name == null || name.isBlank() || name.length() > 30) {
            throw new IllegalArgumentException("표시 이름은 1~30자여야 합니다.");
        }
        Member member = findMember(memberId);
        member.rename(name);
        return profile(member);
    }

    private Member findMember(Long memberId) {
        return members.findById(memberId).orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
    }

    private Profile profile(Member member) {
        return new Profile(member.getId(), member.getEmail(), member.getName());
    }

    public record Profile(Long id, String email, String name) {
    }
}
