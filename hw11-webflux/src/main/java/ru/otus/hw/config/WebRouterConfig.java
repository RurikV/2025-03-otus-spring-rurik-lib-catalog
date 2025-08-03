package ru.otus.hw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.otus.hw.handlers.AuthorHandler;
import ru.otus.hw.handlers.BookPageHandler;
import ru.otus.hw.handlers.GenreHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class WebRouterConfig {

    @Bean
    public RouterFunction<ServerResponse> webRoutes(BookPageHandler bookPageHandler, 
                                                   AuthorHandler authorHandler, 
                                                   GenreHandler genreHandler) {
        return RouterFunctions
                .route(GET("/").and(accept(MediaType.TEXT_HTML)), 
                        request -> ServerResponse.ok()
                                .contentType(MediaType.TEXT_HTML)
                                .bodyValue(getIndexHtml()))
                .andRoute(GET("/api/books").and(accept(MediaType.APPLICATION_JSON)),
                        bookPageHandler::getAllBooks)
                .andRoute(GET("/api/books/{id}").and(accept(MediaType.APPLICATION_JSON)),
                        bookPageHandler::getBook)
                .andRoute(POST("/api/books").and(accept(MediaType.APPLICATION_JSON)),
                        bookPageHandler::createBook)
                .andRoute(PUT("/api/books/{id}").and(accept(MediaType.APPLICATION_JSON)),
                        bookPageHandler::updateBook)
                .andRoute(DELETE("/api/books/{id}").and(accept(MediaType.APPLICATION_JSON)),
                        bookPageHandler::deleteBook)
                .andRoute(GET("/api/authors").and(accept(MediaType.APPLICATION_JSON)),
                        authorHandler::getAllAuthors)
                .andRoute(GET("/api/genres").and(accept(MediaType.APPLICATION_JSON)),
                        genreHandler::getAllGenres)
                .and(RouterFunctions.resources("/js/**", 
                        new ClassPathResource("static/js/")))
                .and(RouterFunctions.resources("/css/**", 
                        new ClassPathResource("static/css/")));
    }
    
    private String getIndexHtml() {
        try {
            return new String(new ClassPathResource("static/index.html")
                    .getInputStream()
                    .readAllBytes());
        } catch (Exception e) {
            return "<html><body><h1>Error loading page</h1></body></html>";
        }
    }
}