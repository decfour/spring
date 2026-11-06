package com.kdj.commerce.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ErrorController())
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @ParameterizedTest
    @CsvSource({"invalid,400", "login,401", "denied,403", "missing,404", "conflict,409", "stock,409", "duplicate,409"})
    @DisplayName("업무 예외에 맞는 HTTP 상태와 안내 화면을 반환한다")
    void businessErrorsSetHtmlStatus(String type, int status) throws Exception {
        mvc.perform(get("/errors/{type}", type))
                .andExpect(status().is(status))
                .andExpect(view().name("error/4xx"))
                .andExpect(model().attribute("errorMessage", "안내 메시지"));
    }

    @Test
    @DisplayName("API 오류는 상태 코드와 JSON 안내를 반환한다")
    void apiGetsJsonEvenWithoutAcceptHeader() throws Exception {
        mvc.perform(get("/api/error"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("대상이 없습니다."));
    }

    @Test
    @DisplayName("JSON을 요청한 화면 오류도 JSON으로 반환한다")
    void jsonAcceptGetsJsonFromPageController() throws Exception {
        mvc.perform(get("/errors/conflict").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @ParameterizedTest
    @CsvSource({"state", "argument"})
    @DisplayName("예상하지 못한 오류는 내부 정보를 숨기고 500으로 처리한다")
    void unexpectedErrorsAreServerErrorsAndHideDetails(String type) throws Exception {
        mvc.perform(get("/errors/{type}", type))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error/5xx"))
                .andExpect(model().attribute("errorMessage", "서버 오류가 발생했습니다."));
    }

    @Controller
    static class ErrorController {
        @GetMapping("/errors/{type}")
        public String fail(@PathVariable String type) {
            throw switch (type) {
                case "invalid" -> new InvalidRequestException("안내 메시지");
                case "login" -> new AuthenticationRequiredException("안내 메시지");
                case "denied" -> new PermissionDeniedException("안내 메시지");
                case "missing" -> new ResourceNotFoundException("안내 메시지");
                case "conflict" -> new BusinessConflictException("안내 메시지");
                case "stock" -> new NotEnoughStockException("안내 메시지");
                case "duplicate" -> new DuplicateMemberException(DuplicateMemberException.Field.EMAIL, "안내 메시지");
                case "argument" -> new IllegalArgumentException("내부 구현 정보");
                default -> new IllegalStateException("파일 저장 경로 등 내부 정보");
            };
        }

        @ResponseBody
        @GetMapping("/api/error")
        public String api() {
            throw new ResourceNotFoundException("대상이 없습니다.");
        }

    }
}
