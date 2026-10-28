package com.kdj.commerce.web.controller;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.exception.DuplicateMemberException;
import com.kdj.commerce.service.*;
import com.kdj.commerce.web.dto.member.SignUpForm;
import com.kdj.commerce.web.security.JwtTokenProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.validation.BeanPropertyBindingResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MemberControllerTest {
    @ParameterizedTest
    @CsvSource({"EMAIL,email", "SIGN_IN_ID,signInId"})
    void duplicateFieldDoesNotDependOnMessage(DuplicateMemberException.Field field, String expectedField) {
        MemberService members = mock(MemberService.class);
        doThrow(new DuplicateMemberException(field, "문구를 변경해도 필드에 표시됩니다."))
                .when(members).signUp(any(Member.class));
        MemberController controller = new MemberController(mock(ItemReviewService.class), members,
                mock(ItemService.class), mock(PurchaseService.class), mock(WalkCourseService.class),
                mock(JwtTokenProvider.class));
        SignUpForm form = new SignUpForm();
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(form, "member");

        assertThat(controller.signUp(form, binding)).isEqualTo("member/signUpForm");
        assertThat(binding.getFieldErrorCount()).isEqualTo(1);
        assertThat(binding.getFieldError(expectedField)).isNotNull();
    }
}
