package com.kdj.commerce.web.argumentresolver;

import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {
    private final MemberService memberService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasLoginAnnotation = parameter.hasParameterAnnotation(Login.class);
        boolean hasMemberType = Member.class.isAssignableFrom(parameter.getParameterType());

        return hasLoginAnnotation && hasMemberType;
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) throws Exception {
        // 1. SecurityContext에 넣어둔 인증 도장 꺼내기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 인증 정보가 없거나, 로그인하지 않은 익명 사용자(anonymousUser)라면 null 반환
        if (authentication == null ||
            !authentication.isAuthenticated() ||
            authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        // 3. 인증 도장에서 사용자의 이메일 꺼내기
        String email = (String) authentication.getPrincipal();
        Member member = memberService.findByEmail(email);

        if (member == null) {
            log.warn("인증된 이메일에 해당하는 회원을 찾을 수 없습니다. email={}", email);
        }

        return memberService.findByEmail(email);
    }
}
