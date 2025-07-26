package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.GenreService;

import java.util.List;

import static org.mockito.BDDMockito.given;

@SpringBootTest
@AutoConfigureWebTestClient
@DisplayName("GenreController should")
class GenreControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GenreService genreService;

    @Test
    @DisplayName("return genres list page")
    void shouldReturnGenresListPage() {
        var genre1 = new Genre("1", "Fantasy");
        var genre2 = new Genre("2", "Science Fiction");
        
        given(genreService.findAll()).willReturn(Flux.fromIterable(List.of(genre1, genre2)));

        webTestClient.get()
                .uri("/genres")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("Fantasy");
                    assert body.contains("Science Fiction");
                    assert body.contains("genre/list") || body.contains("Genres");
                });
    }

    @Test
    @DisplayName("return genres list page with empty list")
    void shouldReturnGenresListPageWithEmptyList() {
        given(genreService.findAll()).willReturn(Flux.empty());

        webTestClient.get()
                .uri("/genres")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("genre/list") || body.contains("Genres") || body.contains("No genres");
                });
    }
}