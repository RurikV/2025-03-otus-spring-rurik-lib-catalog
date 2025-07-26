package ru.otus.hw.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.otus.hw.services.BookService;

import static org.mockito.BDDMockito.given;

@SpringBootTest
@AutoConfigureWebTestClient
class LocalizationIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BookService bookService;

    @Test
    void shouldDisplayRussianTextCorrectly() {
        given(bookService.findAll()).willReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/").queryParam("lang", "ru").build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("Каталог библиотеки");
                    assert body.contains("Книги");
                    assert body.contains("Авторы");
                    assert body.contains("Жанры");
                    assert !body.contains("????");
                });
    }

    @Test
    void shouldDisplayEnglishTextCorrectly() {
        given(bookService.findAll()).willReturn(Flux.empty());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/").queryParam("lang", "en").build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("Library Catalog");
                    assert body.contains("Books");
                    assert body.contains("Authors");
                    assert body.contains("Genres");
                });
    }

    @Test
    void shouldSwitchBetweenLocales() {
        given(bookService.findAll()).willReturn(Flux.empty());

        // Test Russian locale
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/").queryParam("lang", "ru").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("Каталог библиотеки");
                });

        // Test English locale
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/").queryParam("lang", "en").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("Library Catalog");
                });
    }

}