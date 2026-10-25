package com.decfour.dontsell.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignInRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 200) String password)
{
    @Override
    public String toString() {
        return "SignInRequest[credentials=REDACTED]";
    }
}
