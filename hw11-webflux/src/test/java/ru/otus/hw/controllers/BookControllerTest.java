package ru.otus.hw.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest
@AutoConfigureWebTestClient
@DisplayName("BookController should")
class BookControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private GenreService genreService;

    @MockBean
    private CommentService commentService;

    @Test
    @DisplayName("return all books as JSON")
    void shouldReturnAllBooksAsJson() {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var bookDto = new BookDto("1", "Book Title", author, List.of(genre));
        
        given(bookService.findAll()).willReturn(Flux.fromIterable(List.of(bookDto)));

        webTestClient.get()
                .uri("/api/books")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$[0].id").isEqualTo("1")
                .jsonPath("$[0].title").isEqualTo("Book Title");
    }

    @Test
    @DisplayName("return book by id as JSON")
    void shouldReturnBookByIdAsJson() {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var bookDto = new BookDto("1", "Book Title", author, List.of(genre));
        
        given(bookService.findById("1")).willReturn(Mono.just(bookDto));

        webTestClient.get()
                .uri("/api/books/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.title").isEqualTo("Book Title")
                .jsonPath("$.author.fullName").isEqualTo("Author Name");
    }

    @Test
    @DisplayName("return 404 when book not found")
    void shouldReturn404WhenBookNotFound() {
        given(bookService.findById("1")).willThrow(new EntityNotFoundException("Book with id 1 not found"));

        webTestClient.get()
                .uri("/api/books/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("create new book via REST API")
    void shouldCreateNewBookViaRestApi() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var bookDto = new BookDto("1", "Book Title", author, List.of(genre));
        var createDto = new BookCreateDto("Book Title", "1", Set.of("1"));
        
        given(bookService.create(createDto)).willReturn(Mono.just(bookDto));

        webTestClient.post()
                .uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(createDto))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.title").isEqualTo("Book Title")
                .jsonPath("$.author.fullName").isEqualTo("Author Name");

        verify(bookService).create(createDto);
    }

    @Test
    @DisplayName("create new book without genres via REST API")
    void shouldCreateNewBookWithoutGenresViaRestApi() throws Exception {
        var author = new Author("1", "Author Name");
        var bookDto = new BookDto("1", "Book Title", author, List.of());
        var createDto = new BookCreateDto("Book Title", "1", Set.of());
        
        given(bookService.create(createDto)).willReturn(Mono.just(bookDto));

        webTestClient.post()
                .uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(createDto))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.title").isEqualTo("Book Title");

        verify(bookService).create(createDto);
    }

    @Test
    @DisplayName("update book via REST API")
    void shouldUpdateBookViaRestApi() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var bookDto = new BookDto("1", "Updated Title", author, List.of(genre));
        var updateDto = new BookUpdateDto("1", "Updated Title", "1", Set.of("1"));
        
        given(bookService.update(updateDto)).willReturn(Mono.just(bookDto));

        webTestClient.put()
                .uri("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(updateDto))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.title").isEqualTo("Updated Title")
                .jsonPath("$.author.fullName").isEqualTo("Author Name");

        verify(bookService).update(updateDto);
    }

    @Test
    @DisplayName("delete book via REST API")
    void shouldDeleteBookViaRestApi() {
        webTestClient.delete()
                .uri("/api/books/1")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("return 404 when author not found during book creation")
    void shouldReturn404WhenAuthorNotFoundDuringBookCreation() throws Exception {
        var createDto = new BookCreateDto("Book Title", "1", Set.of("1"));
        
        given(bookService.create(createDto)).willThrow(new EntityNotFoundException("Author with id 1 not found"));

        webTestClient.post()
                .uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(createDto))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("return 404 when genre not found during book creation")
    void shouldReturn404WhenGenreNotFoundDuringBookCreation() throws Exception {
        var createDto = new BookCreateDto("Book Title", "1", Set.of("1"));
        
        given(bookService.create(createDto)).willThrow(new EntityNotFoundException("Genre with id 1 not found"));

        webTestClient.post()
                .uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(createDto))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("return 404 when author not found during book update")
    void shouldReturn404WhenAuthorNotFoundDuringBookUpdate() throws Exception {
        var updateDto = new BookUpdateDto("1", "Updated Title", "1", Set.of("1"));
        
        given(bookService.update(updateDto)).willThrow(new EntityNotFoundException("Author with id 1 not found"));

        webTestClient.put()
                .uri("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(updateDto))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("return 404 when genre not found during book update")
    void shouldReturn404WhenGenreNotFoundDuringBookUpdate() throws Exception {
        var updateDto = new BookUpdateDto("1", "Updated Title", "1", Set.of("1"));
        
        given(bookService.update(updateDto)).willThrow(new EntityNotFoundException("Genre with id 1 not found"));

        webTestClient.put()
                .uri("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(updateDto))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("return 400 when genreIds is null during book update")
    void shouldReturn400WhenGenreIdsIsNullDuringBookUpdate() throws Exception {
        var updateDto = new BookUpdateDto("1", "Updated Title", "1", null);
        
        given(bookService.update(updateDto)).willThrow(new IllegalArgumentException("Genre ids must not be null"));

        webTestClient.put()
                .uri("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(updateDto))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("return 400 when genreIds is empty during book update")
    void shouldReturn400WhenGenreIdsIsEmptyDuringBookUpdate() throws Exception {
        var updateDto = new BookUpdateDto("1", "Updated Title", "1", Set.of());
        
        given(bookService.update(updateDto)).willThrow(new IllegalArgumentException("Genre ids must not be empty"));

        webTestClient.put()
                .uri("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(updateDto))
                .exchange()
                .expectStatus().isBadRequest();
    }
}