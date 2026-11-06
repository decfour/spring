package com.kdj.commerce.service;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import com.kdj.commerce.domain.member.MemberType;
import com.kdj.commerce.exception.DuplicateMemberException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MemberServiceTest {
    private final MemberRepository repository = mock(MemberRepository.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private final MemberService service = new MemberService(repository, encoder);

    private Member member(String password) {
        return Member.create("tester", "test@example.com", "tester", password, MemberType.USER);
    }

    @Test
    @DisplayName("회원가입 시 비밀번호를 암호화해 저장한다")
    void signUpStoresHash() {
        Member member = member("password123");
        service.signUp(member);
        assertThat(member.getSignInPassword()).isNotEqualTo("password123");
        assertThat(encoder.matches("password123", member.getSignInPassword())).isTrue();
        verify(repository).save(member);
    }

    @Test
    @DisplayName("암호화된 계정은 올바른 비밀번호로만 로그인한다")
    void hashedAccountOnlyAcceptsCorrectPassword() {
        Member member = member(encoder.encode("password123"));
        String hash = member.getSignInPassword();
        when(repository.findBySignInId("tester")).thenReturn(Optional.of(member));
        assertThat(service.signIn("tester", "wrong")).isNull();
        assertThat(service.signIn("tester", hash)).isNull();
        assertThat(service.signIn("tester", "password123")).isSameAs(member);
        assertThat(member.getSignInPassword()).isEqualTo(hash);
    }

    @Test
    @DisplayName("기존 평문 비밀번호는 로그인 성공 시 암호화한다")
    void legacyAccountIsUpgradedOnlyAfterSuccessfulLogin() {
        Member member = member("password123");
        when(repository.findBySignInId("tester")).thenReturn(Optional.of(member));
        assertThat(service.signIn("tester", "wrong")).isNull();
        assertThat(member.getSignInPassword()).isEqualTo("password123");
        assertThat(service.signIn("tester", "password123")).isSameAs(member);
        assertThat(encoder.matches("password123", member.getSignInPassword())).isTrue();
        assertThat(service.signIn("tester", "password123")).isSameAs(member);
    }

    @Test
    @DisplayName("존재하지 않는 계정은 로그인할 수 없다")
    void missingAccountCannotSignIn() {
        assertThat(service.signIn("missing", "password123")).isNull();
    }

    @Test
    @DisplayName("중복 이메일은 저장하지 않고 이메일 오류로 안내한다")
    void duplicateEmailReportsFieldAndDoesNotSave() {
        Member member = member("password123");
        when(repository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        assertThatThrownBy(() -> service.signUp(member))
                .isInstanceOfSatisfying(DuplicateMemberException.class,
                        ex -> assertThat(ex.getField()).isEqualTo(DuplicateMemberException.Field.EMAIL));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("중복 아이디는 저장하지 않고 아이디 오류로 안내한다")
    void duplicateSignInIdReportsFieldAndDoesNotSave() {
        Member member = member("password123");
        when(repository.findBySignInId(member.getSignInId())).thenReturn(Optional.of(member));
        assertThatThrownBy(() -> service.signUp(member))
                .isInstanceOfSatisfying(DuplicateMemberException.class,
                        ex -> assertThat(ex.getField()).isEqualTo(DuplicateMemberException.Field.SIGN_IN_ID));
        verify(repository, never()).save(any());
    }
}
