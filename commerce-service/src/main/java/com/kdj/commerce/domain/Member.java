package com.kdj.commerce.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class Member {
    private long id;
    private String username;
    private String password;
    private String email;

    public Member() {
    }
}
