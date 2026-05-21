package com.kdj.commerce.domain.member;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Entity
@Data
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "사용자 이름은 필수입니다.")
    private String username;
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "아이디는 필수입니다.")
    @Column(name = "login_id")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Column(name = "login_password")
    private String loginPassword;
}
