package com.decfour.dontsell.auth.service;

import com.decfour.dontsell.auth.dto.SignUpRequest;
import com.decfour.dontsell.auth.dto.SignUpResponse;
import com.decfour.dontsell.auth.JwtTokenProvider;
import com.decfour.dontsell.domain.member.Member;
import com.decfour.dontsell.domain.member.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final String dummyPassword;

    public AuthService(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider
    ) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.dummyPassword = passwordEncoder.encode("unknown-member-password");
    }

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."
            );
        }
        if (memberRepository.existsByAlias(request.alias())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "이미 사용 중인 별명입니다."
            );
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = new Member(
                request.alias(),
                request.email(),
                encodedPassword
        );

        memberRepository.save(member);

        return new SignUpResponse(
                member.getId(),
                member.getAlias(),
                member.getEmail()
        );
    }

    public String signIn(String email, String password) {
        Member member = memberRepository.findByEmail(email).orElse(null);

        String encodedPassword = member == null
                ? dummyPassword
                : member.getPassword();

        boolean matches = passwordEncoder.matches(password, encodedPassword);

        if (member == null || !matches) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "이메일 또는 비밀번호가 올바르지 않습니다."
            );
        }

        return tokenProvider.createToken(member.getId());
    }
}
