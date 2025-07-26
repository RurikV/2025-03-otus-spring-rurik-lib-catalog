package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Author;
import ru.otus.hw.services.AuthorService;

import java.util.List;

import static org.mockito.BDDMockito.given;

@SpringBootTest
@AutoConfigureWebTestClient
@DisplayName("AuthorController should")
class AuthorControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthorService authorService;

    @Test
    @DisplayName("return authors list page")
    void shouldReturnAuthorsListPage() {
        var author1 = new Author("1", "Author One");
        var author2 = new Author("2", "Author Two");
        
        given(authorService.findAll()).willReturn(Flux.fromIterable(List.of(author1, author2)));

        webTestClient.get()
                .uri("/authors")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("Author One");
                    assert body.contains("Author Two");
                    assert body.contains("author/list") || body.contains("Authors");
                });
    }

    @Test
    @DisplayName("return authors list page with empty list")
    void shouldReturnAuthorsListPageWithEmptyList() {
        given(authorService.findAll()).willReturn(Flux.empty());

        webTestClient.get()
                .uri("/authors")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("author/list") || body.contains("Authors") || body.contains("No authors");
                });
    }
}