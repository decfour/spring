package com.kdj.commerce.domain.member;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Member {

    // 회원 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // 회원 이름
    @NotBlank(message = "사용자 이름은 필수입니다.")
    private String username;

    // 이메일
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    // 회원 로그인 아이디
    @NotBlank(message = "아이디는 필수입니다.")
    @Column(name = "login_id")
    private String loginId;

    // 회원 로그인 비밀번호
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Column(name = "login_password")
    private String loginPassword;
}
