package com.kdj.commerce.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
        log.error("IllegalArgumentException 발생: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());

        return "error/4xx";
    }

    @ExceptionHandler(NotEnoughStockException.class)
    public String handleNotEnoughStockException(NotEnoughStockException e, Model model) {
        log.error("NotEnoughStockException 발생 : {}", e.getMessage());
        model.addAttribute("errorMessage", e.getMessage());

        return "error/4xx";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException ex, Model model) {
        log.error("IllegalStateException 발생: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());

        return "error/4xx";
    }

    @ExceptionHandler(Exception.class)
    public String handleAllException(Exception ex, Model model) {
        log.error("예상치 못한 Exception 발생: ", ex);
        model.addAttribute("errorMessage", "오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");

        return "error/4xx";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("정적 리소스를 찾을 수 없습니다: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource Not Found");
    }

    @ExceptionHandler(DuplicateMemberException.class)
    public String handleDuplicateMemberException(DuplicateMemberException ex, Model model) {
        log.error("회원 가입 중복 발생: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/4xx";
    }
}

