package ru.otus.hw.handlers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.config.ApiRouterConfig;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.BookService;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebFluxTest
@Import({ApiRouterConfig.class, BookHandler.class, AuthorHandler.class, GenreHandler.class, CommentHandler.class})
@DisplayName("BookHandler should")
class BookHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BookService bookService;

    @MockBean
    private ru.otus.hw.services.AuthorService authorService;

    @MockBean
    private ru.otus.hw.services.GenreService genreService;

    @MockBean
    private org.springframework.validation.Validator validator;

    @MockBean
    private ru.otus.hw.services.CommentService commentService;

    @Test
    @DisplayName("return all books")
    void shouldReturnAllBooks() {
        var authorDto = new AuthorDto("1", "Author Name");
        var genreDto = new GenreDto("1", "Genre Name");
        var book1 = new BookDto("1", "Book 1", authorDto, List.of(genreDto));
        var book2 = new BookDto("2", "Book 2", authorDto, List.of(genreDto));
        
        given(bookService.findAll()).willReturn(Flux.fromIterable(List.of(book1, book2)));

        webTestClient.get()
                .uri("/api/books")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(BookDto.class)
                .hasSize(2);
    }

    @Test
    @DisplayName("return book by id")
    void shouldReturnBookById() {
        var authorDto = new AuthorDto("1", "Author Name");
        var genreDto = new GenreDto("1", "Genre Name");
        var bookDto = new BookDto("1", "Book Title", authorDto, List.of(genreDto));
        
        given(bookService.findById("1")).willReturn(Mono.just(bookDto));

        webTestClient.get()
                .uri("/api/books/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BookDto.class)
                .isEqualTo(bookDto);
    }

    @Test
    @DisplayName("return 404 when book not found")
    void shouldReturn404WhenBookNotFound() {
        given(bookService.findById("999")).willReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/books/999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("create new book")
    void shouldCreateNewBook() {
        var authorDto = new AuthorDto("1", "Author Name");
        var genreDto = new GenreDto("1", "Genre Name");
        var createDto = new BookCreateDto("New Book", "1", Set.of("1"));
        var createdBook = new BookDto("1", "New Book", authorDto, List.of(genreDto));
        
        given(bookService.create(any(BookCreateDto.class))).willReturn(Mono.just(createdBook));

        webTestClient.post()
                .uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(createDto)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BookDto.class)
                .isEqualTo(createdBook);
    }

    @Test
    @DisplayName("update existing book")
    void shouldUpdateExistingBook() {
        var authorDto = new AuthorDto("1", "Author Name");
        var genreDto = new GenreDto("1", "Genre Name");
        var updateDto = new BookUpdateDto("1", "Updated Book", "1", Set.of("1"));
        var updatedBook = new BookDto("1", "Updated Book", authorDto, List.of(genreDto));
        
        given(bookService.update(any(BookUpdateDto.class))).willReturn(Mono.just(updatedBook));

        webTestClient.put()
                .uri("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BookDto.class)
                .isEqualTo(updatedBook);
    }

    @Test
    @DisplayName("delete book")
    void shouldDeleteBook() {
        given(bookService.deleteById("1")).willReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/books/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("handle validation errors on create")
    void shouldHandleValidationErrorsOnCreate() {
        var createDto = new BookCreateDto("", "", Set.of()); // Invalid data
        
        given(bookService.create(any(BookCreateDto.class)))
                .willReturn(Mono.error(new IllegalArgumentException("Invalid book data")));

        webTestClient.post()
                .uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(createDto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("handle validation errors on update")
    void shouldHandleValidationErrorsOnUpdate() {
        var updateDto = new BookUpdateDto("1", "", "", Set.of()); // Invalid data
        
        given(bookService.update(any(BookUpdateDto.class)))
                .willReturn(Mono.error(new IllegalArgumentException("Invalid book data")));

        webTestClient.put()
                .uri("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("handle server errors gracefully")
    void shouldHandleServerErrorsGracefully() {
        given(bookService.findAll()).willReturn(Flux.error(new RuntimeException("Database error")));

        webTestClient.get()
                .uri("/api/books")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}