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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "이름은 필수입니다.")
    private String username;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일이 아닙니다.")
    private String email;

    @NotBlank(message = "아이디는 필수입니다.")
    @Column(name = "login_id")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Column(name = "login_password")
    private String loginPassword;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", nullable = false)
    private MemberType memberType = MemberType.USER;

    public static Member createMember(String username, String email, String loginId, String loginPassword) {
        Member member = new Member();
        member.setUsername(username);
        member.setEmail(email);
        member.setLoginId(loginId);
        member.setLoginPassword(loginPassword);
        member.setMemberType(MemberType.USER);

        return member;
    }

    public static Member createMemberWithRole(String username, String email, String loginId, String loginPassword, MemberType memberType) {
        Member member = createMember(username, email, loginId, loginPassword);
        member.setMemberType(memberType);
        return member;
    }

    protected Member() {
    }
}