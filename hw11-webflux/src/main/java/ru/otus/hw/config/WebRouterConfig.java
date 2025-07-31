package ru.otus.hw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import ru.otus.hw.handlers.AuthorHandler;
import ru.otus.hw.handlers.BookPageHandler;
import ru.otus.hw.handlers.CommentHandler;
import ru.otus.hw.handlers.GenreHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

@Configuration
public class WebRouterConfig {

    @Bean
    public RouterFunction<ServerResponse> webRoutes(BookPageHandler bookPageHandler, CommentHandler commentHandler, 
                                                   AuthorHandler authorHandler, GenreHandler genreHandler) {
        return bookRoutes(bookPageHandler)
                .and(authorGenreRoutes(authorHandler, genreHandler))
                .and(commentRoutes(commentHandler));
    }

    private RouterFunction<ServerResponse> bookRoutes(BookPageHandler bookPageHandler) {
        return RouterFunctions
                .route(GET("/").and(accept(MediaType.TEXT_HTML)), 
                        bookPageHandler::listBooks)
                .andRoute(GET("/books").and(accept(MediaType.TEXT_HTML)), 
                        bookPageHandler::listBooks)
                .andRoute(GET("/books/new").and(accept(MediaType.TEXT_HTML)), 
                        bookPageHandler::newBookForm)
                .andRoute(GET("/books/{id}/edit").and(accept(MediaType.TEXT_HTML)), 
                        bookPageHandler::editBookForm)
                .andRoute(GET("/books/{id}/delete").and(accept(MediaType.TEXT_HTML)), 
                        bookPageHandler::deleteBookConfirm)
                .andRoute(GET("/books/{id}").and(accept(MediaType.TEXT_HTML)), 
                        bookPageHandler::viewBook)
                .andRoute(POST("/books").and(contentType(MediaType.APPLICATION_FORM_URLENCODED)), 
                        bookPageHandler::createBook)
                .andRoute(POST("/books/{id}").and(contentType(MediaType.APPLICATION_FORM_URLENCODED)), 
                        bookPageHandler::updateBook);
    }

    private RouterFunction<ServerResponse> authorGenreRoutes(AuthorHandler authorHandler, GenreHandler genreHandler) {
        return RouterFunctions
                .route(GET("/authors").and(accept(MediaType.TEXT_HTML)), 
                        authorHandler::listAuthors)
                .andRoute(GET("/genres").and(accept(MediaType.TEXT_HTML)), 
                        genreHandler::listGenres);
    }

    private RouterFunction<ServerResponse> commentRoutes(CommentHandler commentHandler) {
        return RouterFunctions
                .route(GET("/books/{bookId}/comments/new").and(accept(MediaType.TEXT_HTML)), 
                        commentHandler::newCommentForm)
                .andRoute(POST("/books/{bookId}/comments").and(contentType(MediaType.APPLICATION_FORM_URLENCODED)), 
                        commentHandler::saveComment)
                .andRoute(GET("/comments/{id}/edit").and(accept(MediaType.TEXT_HTML)), 
                        commentHandler::editCommentForm)
                .andRoute(POST("/comments/{id}").and(contentType(MediaType.APPLICATION_FORM_URLENCODED)), 
                        commentHandler::updateComment)
                .andRoute(GET("/comments/{id}/delete").and(accept(MediaType.TEXT_HTML)), 
                        commentHandler::deleteCommentConfirm)
                .andRoute(POST("/comments/{id}/delete").and(contentType(MediaType.APPLICATION_FORM_URLENCODED)), 
                        commentHandler::deleteComment);
    }
}