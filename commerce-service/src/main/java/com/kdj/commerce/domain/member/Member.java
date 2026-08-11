package com.kdj.commerce.domain.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10, nullable = false)
    private String username;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "login_id", length = 20, nullable = false, unique = true)
    private String loginId;

    @Column(name = "login_password", length = 255, nullable = false)
    private String loginPassword;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", nullable = false)
    private MemberType memberType = MemberType.USER;

    public static Member create(String username,
                                      String email,
                                      String loginId,
                                      String loginPassword,
                                      MemberType memberType) {
        Member member = new Member();
        member.username = username;
        member.email = email;
        member.loginId = loginId;
        member.loginPassword = loginPassword;
        member.memberType = memberType;

        return member;
    }
}