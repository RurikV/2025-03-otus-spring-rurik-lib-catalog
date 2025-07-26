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
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@WebFluxTest
@Import({CommentHandlerTest.TestConfig.class, CommentHandler.class})
@DisplayName("CommentHandler should")
class CommentHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CommentService commentService;

    @MockBean
    private BookService bookService;

    static class TestConfig {
        @Bean
        public RouterFunction<ServerResponse> commentRoutes(CommentHandler commentHandler) {
            return RouterFunctions
                    // Comment routes
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

    @Test
    @DisplayName("return new comment form page")
    void shouldReturnNewCommentFormPage() {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var bookDto = new BookDto("1", "Book Title", author, List.of(genre));
        
        given(bookService.findById("1")).willReturn(Mono.just(bookDto));

        webTestClient.get()
                .uri("/books/1/comments/new")
                .header("Accept", "text/html")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8");
    }

    @Test
    @DisplayName("redirect to home when book not found for new comment form")
    void shouldRedirectToHomeWhenBookNotFoundForNewCommentForm() {
        given(bookService.findById("999")).willReturn(Mono.empty());

        webTestClient.get()
                .uri("/books/999/comments/new")
                .header("Accept", "text/html")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location("/");
    }

    @Test
    @DisplayName("handle comment creation form submission")
    void shouldHandleCommentCreationFormSubmission() {
        var comment = new Comment("1", "Great book!", "1");
        
        given(commentService.create(any())).willReturn(Mono.just(comment));

        webTestClient.post()
                .uri("/books/1/comments")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("text=Great book!")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location("/books/1");
    }

    @Test
    @DisplayName("redirect to home on comment creation error")
    void shouldRedirectToHomeOnCommentCreationError() {
        given(commentService.create(any())).willReturn(Mono.error(new RuntimeException("Error")));

        webTestClient.post()
                .uri("/books/1/comments")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("text=Great book!")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location("/");
    }

    @Test
    @DisplayName("return edit comment form page")
    void shouldReturnEditCommentFormPage() {
        var comment = new Comment("1", "Great book!", "1");
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var bookDto = new BookDto("1", "Book Title", author, List.of(genre));
        
        given(commentService.findById("1")).willReturn(Mono.just(comment));
        given(bookService.findById("1")).willReturn(Mono.just(bookDto));

        webTestClient.get()
                .uri("/comments/1/edit")
                .header("Accept", "text/html")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8");
    }

    @Test
    @DisplayName("redirect to home when comment not found for edit form")
    void shouldRedirectToHomeWhenCommentNotFoundForEditForm() {
        given(commentService.findById("999")).willReturn(Mono.empty());

        webTestClient.get()
                .uri("/comments/999/edit")
                .header("Accept", "text/html")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location("/");
    }

    @Test
    @DisplayName("handle comment update form submission")
    void shouldHandleCommentUpdateFormSubmission() {
        var originalComment = new Comment("1", "Great book!", "1");
        var updatedComment = new Comment("1", "Updated comment!", "1");
        
        given(commentService.findById("1")).willReturn(Mono.just(originalComment));
        given(commentService.update(any())).willReturn(Mono.just(updatedComment));

        webTestClient.post()
                .uri("/comments/1")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("text=Updated comment!")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location("/books/1");
    }

    @Test
    @DisplayName("redirect to home on comment update error")
    void shouldRedirectToHomeOnCommentUpdateError() {
        given(commentService.findById("1")).willReturn(Mono.error(new RuntimeException("Error")));

        webTestClient.post()
                .uri("/comments/1")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("text=Updated comment!")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location("/");
    }

    @Test
    @DisplayName("return delete comment confirmation page")
    void shouldReturnDeleteCommentConfirmationPage() {
        var comment = new Comment("1", "Great book!", "1");
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var bookDto = new BookDto("1", "Book Title", author, List.of(genre));
        
        given(commentService.findById("1")).willReturn(Mono.just(comment));
        given(bookService.findById("1")).willReturn(Mono.just(bookDto));

        webTestClient.get()
                .uri("/comments/1/delete")
                .header("Accept", "text/html")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8");
    }

    @Test
    @DisplayName("redirect to home when comment not found for delete confirmation")
    void shouldRedirectToHomeWhenCommentNotFoundForDeleteConfirmation() {
        given(commentService.findById("999")).willReturn(Mono.empty());

        webTestClient.get()
                .uri("/comments/999/delete")
                .header("Accept", "text/html")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location("/");
    }

    @Test
    @DisplayName("handle comment deletion")
    void shouldHandleCommentDeletion() {
        var comment = new Comment("1", "Great book!", "1");
        
        given(commentService.findById("1")).willReturn(Mono.just(comment));
        given(commentService.deleteById("1")).willReturn(Mono.empty());

        webTestClient.post()
                .uri("/comments/1/delete")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location("/books/1");
    }

    @Test
    @DisplayName("redirect to home on comment deletion error")
    void shouldRedirectToHomeOnCommentDeletionError() {
        given(commentService.findById("1")).willReturn(Mono.error(new RuntimeException("Error")));

        webTestClient.post()
                .uri("/comments/1/delete")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location("/");
    }
}