package com.kdj.commerce.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateMemberException.class)
    public String handleDuplicateMemberException(DuplicateMemberException ex, Model model) {
        log.warn("회원 중복: {}", ex.getMessage());

        return errorPage(model, ex.getMessage());
    }

    @ExceptionHandler(NotEnoughStockException.class)
    public String handleNotEnoughStockException(NotEnoughStockException e, Model model) {
        log.warn("재고 부족: {}", e.getMessage());

        return errorPage(model, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
        log.warn("잘못된 요청: {}", ex.getMessage());

        return errorPage(model, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(IllegalStateException ex, Model model) {
        log.warn("잘못된 요청: {}", ex.getMessage());

        return errorPage(model, ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFoundException() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(Exception.class)
    public String handleAllException(Exception ex, Model model) {
        log.error("예상치 못한 서버 오류: ", ex);

        return errorPage(model, ex.getMessage());
    }

    private String errorPage(Model model, String message) {
        model.addAttribute("errorMessage", message);

        return errorPage(model, "오류 발생. 잠시 후 다시 시도");
    }
}

