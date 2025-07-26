package ru.otus.hw.handlers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.GenreService;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@WebFluxTest
@Import({GenreHandlerTest.TestConfig.class, GenreHandler.class})
@DisplayName("GenreHandler should")
class GenreHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GenreService genreService;

    static class TestConfig {
        @Bean
        public RouterFunction<ServerResponse> genreRoutes(GenreHandler genreHandler) {
            return RouterFunctions
                    .route(GET("/genres").and(accept(MediaType.TEXT_HTML)), 
                            genreHandler::listGenres);
        }
    }

    @Test
    @DisplayName("return genres list page")
    void shouldReturnGenresListPage() {
        var genre1 = new Genre("1", "Fiction");
        var genre2 = new Genre("2", "Non-Fiction");
        
        given(genreService.findAll()).willReturn(Flux.fromIterable(List.of(genre1, genre2)));

        webTestClient.get()
                .uri("/genres")
                .header("Accept", "text/html")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8");
    }
}