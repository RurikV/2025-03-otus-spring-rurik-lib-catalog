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
import ru.otus.hw.config.WebRouterConfig;
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
@Import({WebRouterConfig.class, BookPageHandler.class, AuthorHandler.class, GenreHandler.class})
@DisplayName("BookPageHandler should")
class BookPageHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BookService bookService;

    @MockBean
    private ru.otus.hw.services.AuthorService authorService;

    @MockBean
    private ru.otus.hw.services.GenreService genreService;

    @Test
    @DisplayName("return index page for root endpoint")
    void shouldReturnIndexPageForRootEndpoint() {
        webTestClient.get()
                .uri("/")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html;charset=UTF-8");
    }

    @Test
    @DisplayName("return all books via AJAX")
    void shouldReturnAllBooksViaAjax() {
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
    @DisplayName("return book by id via AJAX")
    void shouldReturnBookByIdViaAjax() {
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
    @DisplayName("return 404 when book not found via AJAX")
    void shouldReturn404WhenBookNotFoundViaAjax() {
        given(bookService.findById("999")).willReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/books/999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("create new book via AJAX")
    void shouldCreateNewBookViaAjax() {
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
    @DisplayName("update existing book via AJAX")
    void shouldUpdateExistingBookViaAjax() {
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
    @DisplayName("delete book via AJAX")
    void shouldDeleteBookViaAjax() {
        given(bookService.deleteById("1")).willReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/books/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("return all authors via AJAX")
    void shouldReturnAllAuthorsViaAjax() {
        var author1 = new AuthorDto("1", "Author 1");
        var author2 = new AuthorDto("2", "Author 2");
        
        given(authorService.findAll()).willReturn(Flux.fromIterable(List.of(author1, author2)));

        webTestClient.get()
                .uri("/api/authors")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(AuthorDto.class)
                .hasSize(2);
    }

    @Test
    @DisplayName("return all genres via AJAX")
    void shouldReturnAllGenresViaAjax() {
        var genre1 = new GenreDto("1", "Genre 1");
        var genre2 = new GenreDto("2", "Genre 2");
        
        given(genreService.findAll()).willReturn(Flux.fromIterable(List.of(genre1, genre2)));

        webTestClient.get()
                .uri("/api/genres")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(GenreDto.class)
                .hasSize(2);
    }
}