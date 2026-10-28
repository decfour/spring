package com.kdj.commerce.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import java.util.Map;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.info("요청 처리 거절: status={}, message={}", ex.getStatus().value(), ex.getMessage());

        return errorResponse(request, ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFoundException() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(Exception.class)
    public Object handleAllException(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        if (ex instanceof ErrorResponse error) {
            HttpStatusCode status = error.getStatusCode();
            error.getHeaders().forEach((name, values) -> values.forEach(value -> response.addHeader(name, value)));

            if (status.is5xxServerError()) {
                log.error("프레임워크 서버 오류", ex);
            } else {
                log.info("요청 오류: status={}, type={}", status.value(), ex.getClass().getSimpleName());
            }

            return errorResponse(request, status,
                    status.is5xxServerError() ? "서버 오류가 발생했습니다." : "요청을 처리할 수 없습니다.");
        }

        log.error("예상치 못한 서버 오류", ex);

        return errorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
    }

    private Object errorResponse(HttpServletRequest request, HttpStatusCode status, String message) {
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        boolean responseBody = handler instanceof HandlerMethod method
                && (method.hasMethodAnnotation(ResponseBody.class)
                || AnnotatedElementUtils.hasAnnotation(method.getBeanType(), ResponseBody.class));

        boolean acceptsJson = false;
        try {
            String accept = request.getHeader("Accept");
            acceptsJson = accept != null && MediaType.parseMediaTypes(accept).stream()
                    .anyMatch(type -> MediaType.APPLICATION_JSON.isCompatibleWith(type)
                            && !type.isWildcardType() && !type.isWildcardSubtype() && type.getQualityValue() > 0);
        } catch (IllegalArgumentException ignored) {}

        if (responseBody || acceptsJson) {
            return ResponseEntity.status(status).body(Map.of("status", status.value(), "message", message));
        }

        ModelAndView view = new ModelAndView(status.is5xxServerError() ? "error/5xx" : "error/4xx");
        view.setStatus(status);
        view.addObject("errorMessage", message);

        return view;
    }
}

