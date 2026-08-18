package com.kdj.commerce.web.dto.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class registerForm {
    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 10, message = "이름은 2~10자여야 합니다.")
    @Pattern(
            regexp = "^[가-힣a-zA-Z0-9]+$",
            message = "이름은 한글, 영문, 숫자만 사용할 수 있습니다."
    )
    private String username;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일이 아닙니다.")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4~20자여야 합니다.")
    @Pattern(
            regexp = "^[a-zA-Z0-9_]+$",
            message = "아이디는 영문, 숫자, 언더바(_)만 사용할 수 있습니다."
    )
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다.")
    private String loginPassword;
}