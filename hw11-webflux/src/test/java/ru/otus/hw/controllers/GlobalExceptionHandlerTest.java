package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;

import static org.mockito.BDDMockito.given;

@SpringBootTest
@AutoConfigureWebTestClient
@DisplayName("GlobalExceptionHandler should")
class GlobalExceptionHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthorService authorService;

    @Test
    @DisplayName("handle EntityNotFoundException and return 404")
    void shouldHandleEntityNotFoundExceptionAndReturn404() {
        given(authorService.findAll()).willThrow(new EntityNotFoundException("Author not found"));

        webTestClient.get()
                .uri("/authors")
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found")
                .jsonPath("$.message").isEqualTo("Author not found")
                .jsonPath("$.timestamp").exists();
    }

    @Test
    @DisplayName("handle IllegalArgumentException and return 400")
    void shouldHandleIllegalArgumentExceptionAndReturn400() {
        given(authorService.findAll()).willThrow(new IllegalArgumentException("Invalid parameter"));

        webTestClient.get()
                .uri("/authors")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.message").isEqualTo("Invalid parameter")
                .jsonPath("$.timestamp").exists();
    }

    @Test
    @DisplayName("handle generic Exception and return 500")
    void shouldHandleGenericExceptionAndReturn500() {
        given(authorService.findAll()).willThrow(new RuntimeException("Unexpected error"));

        webTestClient.get()
                .uri("/authors")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.error").isEqualTo("Internal Server Error")
                .jsonPath("$.message").isEqualTo("An unexpected error occurred")
                .jsonPath("$.timestamp").exists();
    }
}