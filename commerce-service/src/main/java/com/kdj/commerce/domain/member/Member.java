package com.kdj.commerce.domain.member;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Member {
    private long id;
    private String username;
    private String password;
    private String email;

    public Member() {
    }
}
