package com.kdj.food.auth.service;

import com.kdj.food.entity.Member;
import com.kdj.food.repository.MemberRepository;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberSignUpService {
    private static final String EMAIL_UNIQUE_CONSTRAINT = "uk_member_email";
    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;

    public MemberSignUpService(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder,
            Validator validator
    ) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
    }

    @Transactional
    public Long signUp(String email, String password, String name) {
        String normalizedEmail = MemberEmail.normalize(email);
        validateSignUp(normalizedEmail, password, name);

        if (memberRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException();
        }

        String passwordHash = passwordEncoder.encode(password);
        Member member = new Member(normalizedEmail, passwordHash, name);

        try {
            return memberRepository.saveAndFlush(member).getId();
        } catch (DataIntegrityViolationException exception) {
            if (exception.getCause() instanceof ConstraintViolationException violation) {
                String constraint = violation.getConstraintName();
                if (constraint != null && (constraint.equals(EMAIL_UNIQUE_CONSTRAINT)
                        || constraint.endsWith("." + EMAIL_UNIQUE_CONSTRAINT))) {
                    throw new DuplicateEmailException();
                }
            }
            throw exception;
        }
    }

    private void validateSignUp(String email, String password, String name) {
        if (!validator.validate(new Profile(email, name)).isEmpty()) {
            throw new IllegalArgumentException("이메일 또는 표시 이름 형식이 올바르지 않습니다.");
        }

        if (password == null || password.isBlank()
                || password.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES) {
            throw new IllegalArgumentException("비밀번호는 공백으로만 구성할 수 없으며 UTF-8 기준 72바이트 이하여야 합니다.");
        }
    }

    private record Profile(
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(max = 30) String name
    ) {
    }
}
