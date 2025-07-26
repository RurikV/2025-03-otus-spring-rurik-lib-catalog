package ru.otus.hw.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
class FaviconTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldServeFaviconWithoutError() {
        webTestClient.get()
                .uri("/favicon.ico")
                .exchange()
                .expectStatus().isOk();
    }
}