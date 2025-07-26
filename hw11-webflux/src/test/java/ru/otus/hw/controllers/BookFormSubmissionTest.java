package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

@SpringBootTest
@AutoConfigureWebTestClient
@DisplayName("Book Form Submission Test should")
class BookFormSubmissionTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private GenreService genreService;

    @Test
    @DisplayName("handle POST request to create new book")
    void shouldHandlePostRequestToCreateNewBook() {
        var authors = authorService.findAll().collectList().block();
        var genres = genreService.findAll().collectList().block();
        
        if (authors.isEmpty() || genres.isEmpty()) {
            throw new RuntimeException("No authors or genres found in database");
        }
        
        webTestClient.post()
                .uri("/books")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("title=New Test Book&authorId=" + authors.get(0).getId() + "&genreIds=" + genres.get(0).getId())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/");
    }

    @Test
    @DisplayName("handle POST request to update existing book")
    void shouldHandlePostRequestToUpdateExistingBook() {
        var books = bookService.findAll().collectList().block();
        var authors = authorService.findAll().collectList().block();
        var genres = genreService.findAll().collectList().block();
        
        if (books.isEmpty() || authors.isEmpty() || genres.isEmpty()) {
            throw new RuntimeException("No books, authors or genres found in database");
        }
        
        var existingBook = books.get(0);
        
        webTestClient.post()
                .uri("/books/" + existingBook.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("title=Updated Test Book&authorId=" + authors.get(0).getId() + "&genreIds=" + genres.get(0).getId())
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/books/" + existingBook.getId());
    }

    @Test
    @DisplayName("return 405 Method Not Allowed for unsupported HTTP method")
    void shouldReturn405ForUnsupportedHttpMethod() {
        // Try to PUT to an endpoint that doesn't support PUT (non-API endpoint)
        webTestClient.put()
                .uri("/books/some-id")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("title=Test Book")
                .exchange()
                .expectStatus().isEqualTo(405)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(405)
                .jsonPath("$.error").isEqualTo("Method Not Allowed");
    }

    @Test
    @DisplayName("handle form validation errors gracefully")
    void shouldHandleFormValidationErrorsGracefully() {
        // Try to create book with missing required fields
        webTestClient.post()
                .uri("/books")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("title=&authorId=invalid-author-id&genreIds=invalid-genre-id")
                .exchange()
                .expectStatus().isOk()  // Should return to form with error
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    // Verify that the response contains the form page
                    assert body.contains("book/form") || body.contains("form") || body.contains("error");
                });
    }

    @Test
    @DisplayName("handle update validation errors gracefully")
    void shouldHandleUpdateValidationErrorsGracefully() {
        var books = bookService.findAll().collectList().block();
        if (books.isEmpty()) {
            throw new RuntimeException("No books found in database");
        }
        
        var existingBook = books.get(0);
        
        // Try to update book with invalid data
        webTestClient.post()
                .uri("/books/" + existingBook.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("title=&authorId=invalid-author-id&genreIds=invalid-genre-id")
                .exchange()
                .expectStatus().isOk()  // Should return to form with error
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    // Verify that the response contains the form page
                    assert body.contains("book/form") || body.contains("form") || body.contains("error");
                });
    }
}