package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthorController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("GlobalExceptionHandler should")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthorService authorService;

    @Test
    @DisplayName("handle EntityNotFoundException and return 404")
    void shouldHandleEntityNotFoundExceptionAndReturn404() throws Exception {
        given(authorService.findAll()).willThrow(new EntityNotFoundException("Author not found"));

        mvc.perform(get("/authors"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Author not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("handle IllegalArgumentException and return 400")
    void shouldHandleIllegalArgumentExceptionAndReturn400() throws Exception {
        given(authorService.findAll()).willThrow(new IllegalArgumentException("Invalid parameter"));

        mvc.perform(get("/authors"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid parameter"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("handle generic Exception and return 500")
    void shouldHandleGenericExceptionAndReturn500() throws Exception {
        given(authorService.findAll()).willThrow(new RuntimeException("Unexpected error"));

        mvc.perform(get("/authors"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}