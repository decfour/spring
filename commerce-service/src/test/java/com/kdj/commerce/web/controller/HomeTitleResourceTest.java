package com.kdj.commerce.web.controller;

import com.kdj.commerce.web.file.FileStore;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HomeTitleResourceTest {
    @Test
    void titleIsServedAsStaticResourceWithoutUsingUploadController() throws Exception {
        try (var context = new AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new MockServletContext());
            context.register(ResourceConfig.class);
            context.refresh();
            var mvc = MockMvcBuilders.webAppContextSetup(context).build();
            mvc.perform(get("/images/branding/title.png"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("image/png"));
            verifyNoInteractions(context.getBean(FileStore.class));
        }
    }

    @Configuration
    @EnableWebMvc
    static class ResourceConfig implements WebMvcConfigurer {
        @Bean
        FileStore fileStore() {
            return mock(FileStore.class);
        }

        @Bean
        FileController fileController(FileStore fileStore) {
            return new FileController(fileStore);
        }

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
        }
    }
}
