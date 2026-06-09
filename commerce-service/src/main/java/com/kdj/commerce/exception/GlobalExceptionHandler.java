package com.kdj.commerce.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. 상품 재고 부족 예외 처리
    @ExceptionHandler(NotEnoughStockException.class)
    public String handleNotEnoughStockException(NotEnoughStockException e, Model model) {
        log.error("NotEnoughStockException 발생 : {}", e.getMessage());

        // 화면에 에러 메시지 전달
        model.addAttribute("errorMessage", e.getMessage());

        // 에러 전용 Thymeleaf 뷰, 기존 페이지로 이동
        return "error/businessError";
    }

    // 2. 잘못된 파라미터 요청 및 비즈니스 제약 조건 위반 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, Model model) {
        log.warn("IllegalArgumentException 발생: {}", e.getMessage());

        model.addAttribute("errorMessage", e.getMessage());

        return "error/businessError";
    }

    /*
    // 3. 서버 내부 예상치 못한 심각한 에러 처리
    @ExceptionHandler(Exception.class)
    public String handleAllException(Exception e, Model model) {
        log.error("예상치 못한 에러 발생", e);

        model.addAttribute("errorMessage", "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요.");

        return "error/systemError";
    }
    */

}
