package ru.otus.hw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.otus.hw.handlers.AuthorHandler;
import ru.otus.hw.handlers.BookHandler;
import ru.otus.hw.handlers.GenreHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class ApiRouterConfig {

    @Bean
    public RouterFunction<ServerResponse> apiRoutes(BookHandler bookHandler, 
                                                   AuthorHandler authorHandler, 
                                                   GenreHandler genreHandler) {
        return RouterFunctions
                .route(GET("/api/books").and(accept(MediaType.APPLICATION_JSON)), 
                        bookHandler::getAllBooks)
                .andRoute(GET("/api/books/{id}").and(accept(MediaType.APPLICATION_JSON)), 
                        bookHandler::getBook)
                .andRoute(POST("/api/books").and(accept(MediaType.APPLICATION_JSON)), 
                        bookHandler::createBook)
                .andRoute(PUT("/api/books/{id}").and(accept(MediaType.APPLICATION_JSON)), 
                        bookHandler::updateBook)
                .andRoute(DELETE("/api/books/{id}").and(accept(MediaType.APPLICATION_JSON)), 
                        bookHandler::deleteBook)
                .andRoute(GET("/api/authors").and(accept(MediaType.APPLICATION_JSON)), 
                        authorHandler::getAllAuthors)
                .andRoute(GET("/api/genres").and(accept(MediaType.APPLICATION_JSON)), 
                        genreHandler::getAllGenres);
    }
}