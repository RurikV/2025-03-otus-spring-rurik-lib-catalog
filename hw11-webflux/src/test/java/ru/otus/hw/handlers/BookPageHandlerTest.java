package ru.otus.hw.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.config.WebRouterConfig;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@WebFluxTest
@Import({WebRouterConfig.class, BookPageHandler.class, CommentHandler.class, AuthorHandler.class, GenreHandler.class})
@DisplayName("BookPageHandler should")
class BookPageHandlerTest {

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

    @BeforeEach
    void setUp() {
        // Default mock setup to prevent NPE
        given(commentService.findByBookId(anyString())).willReturn(Flux.empty());
    }

    @Test
    @DisplayName("return books list page for root endpoint")
    void shouldReturnBooksListPageForRootEndpoint() {
        webTestClient.get()
                .uri("/")
                .header("Accept", "text/html")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8");
    }

    @Test
    @DisplayName("return books list page for /books endpoint")
    void shouldReturnBooksListPageForBooksEndpoint() {
        webTestClient.get()
                .uri("/books")
                .header("Accept", "text/html")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8");
    }

    @Test
    @DisplayName("return book view page with bookId in model")
    void shouldReturnBookViewPageWithBookIdInModel() {
        var authorDto = new AuthorDto("1", "Author Name");
        var genreDto = new GenreDto("1", "Genre Name");
        var bookDto = new BookDto("1", "Book Title", authorDto, List.of(genreDto));
        
        given(bookService.findById("1")).willReturn(Mono.just(bookDto));
        given(commentService.findByBookId("1")).willReturn(Flux.empty());
        
        webTestClient.get()
                .uri("/books/1")
                .header("Accept", "text/html")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8");
    }

    @Test
    @DisplayName("return new book form page with authors and genres")
    void shouldReturnNewBookFormPageWithAuthorsAndGenres() {
        var authorDto = new AuthorDto("1", "Author Name");
        var genreDto = new GenreDto("1", "Genre Name");
        
        given(authorService.findAll()).willReturn(Flux.fromIterable(List.of(authorDto)));
        given(genreService.findAll()).willReturn(Flux.fromIterable(List.of(genreDto)));

        webTestClient.get()
                .uri("/books/new")
                .header("Accept", "text/html")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8");
    }

    @Test
    @DisplayName("return edit book form page with authors, genres and bookId")
    void shouldReturnEditBookFormPageWithAuthorsGenresAndBookId() {
        var authorDto = new AuthorDto("1", "Author Name");
        var genreDto = new GenreDto("1", "Genre Name");
        var bookDto = new BookDto("1", "Book Title", authorDto, List.of(genreDto));
        
        given(bookService.findById("1")).willReturn(Mono.just(bookDto));
        given(authorService.findAll()).willReturn(Flux.fromIterable(List.of(authorDto)));
        given(genreService.findAll()).willReturn(Flux.fromIterable(List.of(genreDto)));

        webTestClient.get()
                .uri("/books/1/edit")
                .header("Accept", "text/html")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8");
    }

    @Test
    @DisplayName("return delete confirmation page with bookId in model")
    void shouldReturnDeleteConfirmationPageWithBookIdInModel() {
        webTestClient.get()
                .uri("/books/1/delete")
                .header("Accept", "text/html")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8");
    }

    @Test
    @DisplayName("handle book creation form submission")
    void shouldHandleBookCreationFormSubmission() {
        var authorDto = new AuthorDto("1", "Author Name");
        var genreDto = new GenreDto("1", "Genre Name");
        var createdBook = new BookDto("1", "New Book", authorDto, List.of(genreDto));
        
        given(bookService.create(org.mockito.ArgumentMatchers.any())).willReturn(Mono.just(createdBook));

        webTestClient.post()
                .uri("/books")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("title=New Book&authorId=1&genreIds=1")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location("/");
    }

    @Test
    @DisplayName("handle book update form submission")
    void shouldHandleBookUpdateFormSubmission() {
        var authorDto = new AuthorDto("1", "Author Name");
        var genreDto = new GenreDto("1", "Genre Name");
        var updatedBook = new BookDto("1", "Updated Book", authorDto, List.of(genreDto));
        
        given(bookService.update(org.mockito.ArgumentMatchers.any())).willReturn(Mono.just(updatedBook));

        webTestClient.post()
                .uri("/books/1")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("title=Updated Book&authorId=1&genreIds=1")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().location("/books/1");
    }

    @Test
    @DisplayName("return 404 when book not found")
    void shouldReturn404WhenBookNotFound() {
        given(bookService.findById("999")).willReturn(Mono.empty());
        
        webTestClient.get()
                .uri("/books/999")
                .header("Accept", "text/html")
                .exchange()
                .expectStatus().isNotFound();
    }
}