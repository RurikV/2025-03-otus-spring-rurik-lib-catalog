package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest
@AutoConfigureWebTestClient
@DisplayName("CommentController should")
class CommentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CommentService commentService;

    @MockBean
    private BookService bookService;

    @Test
    @DisplayName("return new comment form")
    void shouldReturnNewCommentForm() {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var bookDto = new BookDto("1", "Book Title", author, List.of(genre));
        
        given(bookService.findById("1")).willReturn(Mono.just(bookDto));

        webTestClient.get()
                .uri("/books/1/comments/new")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("comment/form") || body.contains("form");
                });
    }

    @Test
    @DisplayName("redirect to home when book not found for new comment form")
    void shouldRedirectToHomeWhenBookNotFoundForNewCommentForm() {
        given(bookService.findById("1")).willReturn(Mono.error(new EntityNotFoundException("Book with id 1 not found")));

        webTestClient.get()
                .uri("/books/1/comments/new")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/");
    }

    @Test
    @DisplayName("save comment and redirect to book view")
    void shouldSaveCommentAndRedirectToBookView() {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Book Title", author, List.of(genre));
        var comment = new Comment("1", "Comment text", book.getId());
        
        var createDto = new CommentCreateDto("Comment text", "1");
        given(commentService.create(createDto)).willReturn(Mono.just(comment));

        webTestClient.post()
                .uri("/books/1/comments")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("text=Comment text")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/books/1");

        verify(commentService).create(createDto);
    }

    @Test
    @DisplayName("return edit comment form")
    void shouldReturnEditCommentForm() {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Book Title", author, List.of(genre));
        var comment = new Comment("1", "Comment text", book.getId());
        comment.setBook(book); // Set the book field for template access
        
        given(commentService.findById("1")).willReturn(Mono.just(comment));

        webTestClient.get()
                .uri("/comments/1/edit")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.toLowerCase().contains("edit");
                });
    }

    @Test
    @DisplayName("redirect to home when comment not found for edit form")
    void shouldRedirectToHomeWhenCommentNotFoundForEditForm() {
        given(commentService.findById("1")).willReturn(Mono.error(new EntityNotFoundException("Comment with id 1 not found")));

        webTestClient.get()
                .uri("/comments/1/edit")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/");
    }

    @Test
    @DisplayName("update comment and redirect to book view")
    void shouldUpdateCommentAndRedirectToBookView() {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Book Title", author, List.of(genre));
        var comment = new Comment("1", "Updated comment text", book.getId());
        comment.setBook(book); // Set the book field for template access
        
        given(commentService.findById("1")).willReturn(Mono.just(comment));
        var updateDto = new CommentUpdateDto("1", "Updated comment text");
        given(commentService.update(updateDto)).willReturn(Mono.just(comment));

        webTestClient.post()
                .uri("/comments/1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("text=Updated comment text")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/books/1");

        verify(commentService).update(updateDto);
    }

    @Test
    @DisplayName("redirect to home when comment not found for update")
    void shouldRedirectToHomeWhenCommentNotFoundForUpdate() {
        given(commentService.findById("1")).willReturn(Mono.error(new EntityNotFoundException("Comment with id 1 not found")));

        webTestClient.post()
                .uri("/comments/1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("text=Updated comment text")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/");
    }

    @Test
    @DisplayName("return delete comment confirmation page")
    void shouldReturnDeleteCommentConfirmationPage() {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Book Title", author, List.of(genre));
        var comment = new Comment("1", "Comment text", book.getId());
        comment.setBook(book); // Set the book field for template access
        
        given(commentService.findById("1")).willReturn(Mono.just(comment));

        webTestClient.get()
                .uri("/comments/1/delete")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("comment/delete") || body.contains("delete");
                });
    }

    @Test
    @DisplayName("redirect to home when comment not found for delete confirmation")
    void shouldRedirectToHomeWhenCommentNotFoundForDeleteConfirmation() {
        given(commentService.findById("1")).willReturn(Mono.error(new EntityNotFoundException("Comment with id 1 not found")));

        webTestClient.get()
                .uri("/comments/1/delete")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/");
    }

    @Test
    @DisplayName("delete comment and redirect to book view")
    void shouldDeleteCommentAndRedirectToBookView() {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Book Title", author, List.of(genre));
        var comment = new Comment("1", "Comment text", book.getId());
        comment.setBook(book); // Set the book field for template access
        
        given(commentService.findById("1")).willReturn(Mono.just(comment));
        given(commentService.deleteById("1")).willReturn(Mono.empty());

        webTestClient.post()
                .uri("/comments/1/delete")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/books/1");

        verify(commentService).deleteById("1");
    }

    @Test
    @DisplayName("redirect to home when comment not found for delete")
    void shouldRedirectToHomeWhenCommentNotFoundForDelete() {
        given(commentService.findById("1")).willReturn(Mono.error(new EntityNotFoundException("Comment with id 1 not found")));

        webTestClient.post()
                .uri("/comments/1/delete")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/");
    }
}