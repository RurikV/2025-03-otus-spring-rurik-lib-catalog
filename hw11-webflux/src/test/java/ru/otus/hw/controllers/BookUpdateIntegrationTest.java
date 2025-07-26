package ru.otus.hw.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.services.BookService;

import java.util.Set;

@SpringBootTest
@AutoConfigureWebTestClient
@DisplayName("Book Update Integration Test should")
class BookUpdateIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookService bookService;

    @Test
    @DisplayName("reproduce 500 error when updating book with null genreIds")
    void shouldReproduce500ErrorWithNullGenreIds() throws Exception {
        // Get an existing book
        var books = bookService.findAll().collectList().block();
        if (books.isEmpty()) {
            throw new RuntimeException("No books found in database");
        }
        var existingBook = books.get(0);
        
        var updateDto = new BookUpdateDto(existingBook.getId(), "Updated Title", existingBook.getAuthor().getId(), null);
        
        webTestClient.put()
                .uri("/api/books/" + existingBook.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(updateDto))
                .exchange()
                .expectStatus().isBadRequest(); // Should be 400, not 500
    }

    @Test
    @DisplayName("reproduce 500 error when updating book with empty genreIds")
    void shouldReproduce500ErrorWithEmptyGenreIds() throws Exception {
        // Get an existing book
        var books = bookService.findAll().collectList().block();
        if (books.isEmpty()) {
            throw new RuntimeException("No books found in database");
        }
        var existingBook = books.get(0);
        
        var updateDto = new BookUpdateDto(existingBook.getId(), "Updated Title", existingBook.getAuthor().getId(), Set.of());
        
        webTestClient.put()
                .uri("/api/books/" + existingBook.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(updateDto))
                .exchange()
                .expectStatus().isBadRequest(); // Should be 400, not 500
    }

    @Test
    @DisplayName("reproduce 500 error when updating book with invalid author")
    void shouldReproduce500ErrorWithInvalidAuthor() throws Exception {
        // Get an existing book
        var books = bookService.findAll().collectList().block();
        if (books.isEmpty()) {
            throw new RuntimeException("No books found in database");
        }
        var existingBook = books.get(0);
        
        var updateDto = new BookUpdateDto(existingBook.getId(), "Updated Title", "invalid-author-id", Set.of(existingBook.getGenres().get(0).getId()));
        
        webTestClient.put()
                .uri("/api/books/" + existingBook.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(updateDto))
                .exchange()
                .expectStatus().isNotFound(); // Should be 404, not 500
    }

    @Test
    @DisplayName("reproduce 500 error when updating book with invalid genre")
    void shouldReproduce500ErrorWithInvalidGenre() throws Exception {
        // Get an existing book
        var books = bookService.findAll().collectList().block();
        if (books.isEmpty()) {
            throw new RuntimeException("No books found in database");
        }
        var existingBook = books.get(0);
        
        var updateDto = new BookUpdateDto(existingBook.getId(), "Updated Title", existingBook.getAuthor().getId(), Set.of("invalid-genre-id"));
        
        webTestClient.put()
                .uri("/api/books/" + existingBook.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(updateDto))
                .exchange()
                .expectStatus().isNotFound(); // Should be 404, not 500
    }

    @Test
    @DisplayName("reproduce 500 error with malformed JSON")
    void shouldReproduce500ErrorWithMalformedJson() throws Exception {
        // Get an existing book
        var books = bookService.findAll().collectList().block();
        if (books.isEmpty()) {
            throw new RuntimeException("No books found in database");
        }
        var existingBook = books.get(0);
        
        String malformedJson = "{\"title\":\"Updated Title\",\"authorId\":\"" + existingBook.getAuthor().getId() + "\",\"genreIds\":[\"" + existingBook.getGenres().get(0).getId() + "\""; // Missing closing bracket
        
        webTestClient.put()
                .uri("/api/books/" + existingBook.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(malformedJson)
                .exchange()
                .expectStatus().is5xxServerError(); // This might cause 500
    }

    @Test
    @DisplayName("reproduce 500 error with validation failure")
    void shouldReproduce500ErrorWithValidationFailure() throws Exception {
        // Get an existing book
        var books = bookService.findAll().collectList().block();
        if (books.isEmpty()) {
            throw new RuntimeException("No books found in database");
        }
        var existingBook = books.get(0);
        
        // Create DTO with blank title (should fail @NotBlank validation)
        var updateDto = new BookUpdateDto(existingBook.getId(), "", existingBook.getAuthor().getId(), Set.of(existingBook.getGenres().get(0).getId()));
        
        webTestClient.put()
                .uri("/api/books/" + existingBook.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(updateDto))
                .exchange()
                .expectStatus().isBadRequest(); // Should be 400 now with the fix
    }

    @Test
    @DisplayName("return 400 for validation failure with blank authorId")
    void shouldReturn400ForBlankAuthorId() throws Exception {
        // Get an existing book
        var books = bookService.findAll().collectList().block();
        if (books.isEmpty()) {
            throw new RuntimeException("No books found in database");
        }
        var existingBook = books.get(0);
        
        // Create DTO with blank authorId (should fail @NotBlank validation)
        var updateDto = new BookUpdateDto(existingBook.getId(), "Updated Title", "", Set.of(existingBook.getGenres().get(0).getId()));
        
        webTestClient.put()
                .uri("/api/books/" + existingBook.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(updateDto))
                .exchange()
                .expectStatus().isBadRequest(); // Should be 400 now with the fix
    }
}