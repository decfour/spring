package com.decfour.dontsell.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SecurityConfigTests.HomeController.class)
@Import({SecurityConfig.class, SecurityConfigTests.HomeController.class})
class SecurityConfigTests {
    @Autowired
    private MockMvc mvc;

    @Test
    void homeIsPublic() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("home"));
    }

    @Test
    void otherPagesRequireLogin() throws Exception {
        mvc.perform(get("/private").accept("text/html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void missingStaticResourceDoesNotRequireLogin() throws Exception {
        mvc.perform(get("/css/missing.css"))
                .andExpect(status().isNotFound());
    }

    @RestController
    static class HomeController {
        @GetMapping("/")
        String home() {
            return "home";
        }
    }
}
