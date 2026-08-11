package com.kdj.tarot.domain.member;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private MemberType memberType;

    private Integer level;
    private Integer exp;

    protected Member() {
    }
}
