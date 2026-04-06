package com.kdj.commerce.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Member {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String role;                                // user/admin
    private LocalDateTime createdAt;

    public Member() {
    }
}
