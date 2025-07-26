package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;

import static org.mockito.BDDMockito.given;

@SpringBootTest
@AutoConfigureWebTestClient
@DisplayName("BookPageController should")
class BookPageControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private GenreService genreService;

    @MockBean
    private BookService bookService;

    @MockBean
    private CommentService commentService;

    @Test
    @DisplayName("return books list page for root endpoint")
    void shouldReturnBooksListPageForRootEndpoint() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("book/list") || body.contains("Books");
                });
    }

    @Test
    @DisplayName("return books list page for /books endpoint")
    void shouldReturnBooksListPageForBooksEndpoint() {
        webTestClient.get()
                .uri("/books")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("book/list") || body.contains("Books");
                });
    }

    @Test
    @DisplayName("return book view page with bookId in model")
    void shouldReturnBookViewPageWithBookIdInModel() {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var bookDto = new BookDto("1", "Book Title", author, List.of(genre));
        
        given(bookService.findById("1")).willReturn(Mono.just(bookDto));
        given(commentService.findByBookId("1")).willReturn(Flux.empty());
        
        webTestClient.get()
                .uri("/books/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("book-details") && body.contains("data-book-id=\"1\"");
                });
    }

    @Test
    @DisplayName("return new book form page with authors and genres")
    void shouldReturnNewBookFormPageWithAuthorsAndGenres() {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        
        given(authorService.findAll()).willReturn(Flux.fromIterable(List.of(author)));
        given(genreService.findAll()).willReturn(Flux.fromIterable(List.of(genre)));

        webTestClient.get()
                .uri("/books/new")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("book/form") || body.contains("Author Name");
                    assert body.contains("Genre Name");
                });
    }

    @Test
    @DisplayName("return edit book form page with authors, genres and bookId")
    void shouldReturnEditBookFormPageWithAuthorsGenresAndBookId() {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var bookDto = new BookDto("1", "Book Title", author, List.of(genre));
        
        given(authorService.findAll()).willReturn(Flux.fromIterable(List.of(author)));
        given(genreService.findAll()).willReturn(Flux.fromIterable(List.of(genre)));
        given(bookService.findById("1")).willReturn(Mono.just(bookDto));

        webTestClient.get()
                .uri("/books/1/edit")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("book/form") || body.contains("Author Name");
                    assert body.contains("Genre Name");
                    assert body.contains("Book Title");
                });
    }

    @Test
    @DisplayName("return delete confirmation page with bookId in model")
    void shouldReturnDeleteConfirmationPageWithBookIdInModel() {
        webTestClient.get()
                .uri("/books/1/delete")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("book/delete") || body.contains("delete");
                });
    }

    @Test
    @DisplayName("handle different book IDs correctly")
    void shouldHandleDifferentBookIdsCorrectly() {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var bookDto123 = new BookDto("123", "Book Title 123", author, List.of(genre));
        var bookDtoAbc = new BookDto("abc", "Book Title abc", author, List.of(genre));
        
        given(bookService.findById("123")).willReturn(Mono.just(bookDto123));
        given(bookService.findById("abc")).willReturn(Mono.just(bookDtoAbc));
        given(commentService.findByBookId("123")).willReturn(Flux.empty());
        given(commentService.findByBookId("abc")).willReturn(Flux.empty());
        given(authorService.findAll()).willReturn(Flux.fromIterable(List.of(author)));
        given(genreService.findAll()).willReturn(Flux.fromIterable(List.of(genre)));
        
        // Test with different book ID
        webTestClient.get()
                .uri("/books/123")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("book-details") && body.contains("data-book-id=\"123\"");
                });

        webTestClient.get()
                .uri("/books/abc/edit")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("book/form") || body.contains("Book Title abc");
                });

        webTestClient.get()
                .uri("/books/xyz/delete")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("book/delete") || body.contains("delete");
                });
    }

    @Test
    @DisplayName("call services correctly for form pages")
    void shouldCallServicesCorrectlyForFormPages() {
        var authors = List.of(new Author("1", "Author 1"), new Author("2", "Author 2"));
        var genres = List.of(new Genre("1", "Genre 1"), new Genre("2", "Genre 2"));
        var bookDto = new BookDto("1", "Book Title", authors.get(0), genres);
        
        given(authorService.findAll()).willReturn(Flux.fromIterable(authors));
        given(genreService.findAll()).willReturn(Flux.fromIterable(genres));
        given(bookService.findById("1")).willReturn(Mono.just(bookDto));

        // Test new book form
        webTestClient.get()
                .uri("/books/new")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("Author 1") || body.contains("Author 2");
                    assert body.contains("Genre 1") || body.contains("Genre 2");
                });

        // Test edit book form
        webTestClient.get()
                .uri("/books/1/edit")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8")
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("Author 1") || body.contains("Author 2");
                    assert body.contains("Genre 1") || body.contains("Genre 2");
                });
    }
}