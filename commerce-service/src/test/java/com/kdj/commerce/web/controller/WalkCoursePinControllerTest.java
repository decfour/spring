package com.kdj.commerce.web.controller;

import com.kdj.commerce.exception.GlobalExceptionHandler;
import com.kdj.commerce.service.WalkCoursePinService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WalkCoursePinControllerTest {
    private final WalkCoursePinService service = mock(WalkCoursePinService.class);

    @Test
    void invalidInputNeverReachesService() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new WalkCoursePinController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        for (String json : List.of(
                "{\"title\":\" \",\"content\":\"hello\",\"latitude\":37.5,\"longitude\":127}",
                "{\"title\":\"hi\",\"content\":\"hello\",\"latitude\":91,\"longitude\":127}",
                "{\"title\":\"hi\",\"content\":\"hello\"}")) {
            mvc.perform(post("/walk/course/10/pins").contentType(MediaType.APPLICATION_JSON).content(json))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }
}
