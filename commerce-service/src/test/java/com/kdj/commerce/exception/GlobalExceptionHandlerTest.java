package com.kdj.commerce.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

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
    void businessErrorsSetHtmlStatus(String type, int status) throws Exception {
        mvc.perform(get("/errors/{type}", type))
                .andExpect(status().is(status))
                .andExpect(view().name("error/4xx"))
                .andExpect(model().attribute("errorMessage", "안내 메시지"));
    }

    @Test
    void apiGetsJsonEvenWithoutAcceptHeader() throws Exception {
        mvc.perform(get("/api/error"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("대상이 없습니다."));
    }

    @Test
    void jsonAcceptGetsJsonFromPageController() throws Exception {
        mvc.perform(get("/errors/conflict").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @ParameterizedTest
    @CsvSource({"state", "argument"})
    void unexpectedErrorsAreServerErrorsAndHideDetails(String type) throws Exception {
        mvc.perform(get("/errors/{type}", type))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error/5xx"))
                .andExpect(model().attribute("errorMessage", "서버 오류가 발생했습니다."));
    }

    @Test
    void frameworkNotFoundIsNotChangedToServerError() throws Exception {
        mvc.perform(get("/errors/framework"))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingParameterKeepsBadRequestStatus() throws Exception {
        mvc.perform(get("/parameter"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void wrongHttpMethodKeepsStatusAndAllowHeader() throws Exception {
        mvc.perform(post("/parameter"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", org.hamcrest.Matchers.containsString("GET")));
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
                case "framework" -> new ResponseStatusException(HttpStatus.NOT_FOUND);
                case "argument" -> new IllegalArgumentException("내부 구현 정보");
                default -> new IllegalStateException("파일 저장 경로 등 내부 정보");
            };
        }

        @ResponseBody
        @GetMapping("/api/error")
        public String api() {
            throw new ResourceNotFoundException("대상이 없습니다.");
        }

        @GetMapping("/parameter")
        public String parameter(@RequestParam Long id) {
            return "unused";
        }
    }
}
