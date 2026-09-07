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
    private String name;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "sign_in_id", length = 20, nullable = false, unique = true)
    private String signInId;

    @Column(name = "sign_in_password", length = 255, nullable = false)
    private String signInPassword;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", nullable = false)
    private MemberType memberType = MemberType.USER;

    public static Member create(String name,
                                      String email,
                                      String signInId,
                                      String signInPassword,
                                      MemberType memberType) {
        Member member = new Member();
        member.name = name;
        member.email = email;
        member.signInId = signInId;
        member.signInPassword = signInPassword;
        member.memberType = memberType;

        return member;
    }
}