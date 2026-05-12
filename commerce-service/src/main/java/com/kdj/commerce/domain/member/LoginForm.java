package com.kdj.commerce.domain.member;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

@Data
public class LoginForm {

    @NotEmpty
    private String loginId;
    @NotEmpty
    private String loginPassword;
}
