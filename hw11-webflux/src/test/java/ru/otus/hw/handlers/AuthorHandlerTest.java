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
import ru.otus.hw.models.Author;
import ru.otus.hw.services.AuthorService;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@WebFluxTest
@Import({AuthorHandlerTest.TestConfig.class, AuthorHandler.class})
@DisplayName("AuthorHandler should")
class AuthorHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthorService authorService;

    static class TestConfig {
        @Bean
        public RouterFunction<ServerResponse> authorRoutes(AuthorHandler authorHandler) {
            return RouterFunctions
                    .route(GET("/authors").and(accept(MediaType.TEXT_HTML)), 
                            authorHandler::listAuthors);
        }
    }

    @Test
    @DisplayName("return authors list page")
    void shouldReturnAuthorsListPage() {
        var author1 = new Author("1", "Author One");
        var author2 = new Author("2", "Author Two");
        
        given(authorService.findAll()).willReturn(Flux.fromIterable(List.of(author1, author2)));

        webTestClient.get()
                .uri("/authors")
                .header("Accept", "text/html")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8");
    }
}