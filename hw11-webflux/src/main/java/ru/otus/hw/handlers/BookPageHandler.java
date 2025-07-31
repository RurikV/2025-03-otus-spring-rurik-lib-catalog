package ru.otus.hw.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;

import static ru.otus.hw.handlers.ErrorHandlingUtils.handlePageErrors;

@Component
@RequiredArgsConstructor
public class BookPageHandler {

    private final AuthorService authorService;

    private final GenreService genreService;

    private final BookService bookService;

    private final CommentService commentService;

    public Mono<ServerResponse> listBooks(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .render("book/list");
    }

    public Mono<ServerResponse> viewBook(ServerRequest request) {
        String id = request.pathVariable("id");
        return Mono.zip(bookService.findById(id), commentService.findByBookId(id).collectList())
                .flatMap(tuple -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .render("book/view", Map.of(
                                "bookId", id,
                                "book", tuple.getT1(),
                                "comments", tuple.getT2()
                        )))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(handlePageErrors("/"));
    }

    public Mono<ServerResponse> newBookForm(ServerRequest request) {
        return Mono.zip(authorService.findAll().collectList(), genreService.findAll().collectList())
                .flatMap(tuple -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .render("book/form", Map.of(
                                "book", new BookDto(),
                                "authors", tuple.getT1(),
                                "genres", tuple.getT2()
                        )))
                .onErrorResume(handlePageErrors("/"));
    }

    public Mono<ServerResponse> editBookForm(ServerRequest request) {
        String id = request.pathVariable("id");
        return Mono.zip(
                bookService.findById(id),
                authorService.findAll().collectList(),
                genreService.findAll().collectList()
        ).flatMap(tuple -> ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .render("book/form", Map.of(
                        "bookId", id,
                        "book", tuple.getT1(),
                        "authors", tuple.getT2(),
                        "genres", tuple.getT3()
                )))
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(handlePageErrors("/"));
    }

    public Mono<ServerResponse> deleteBookConfirm(ServerRequest request) {
        String id = request.pathVariable("id");
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .render("book/delete", Map.of("bookId", id));
    }

    public Mono<ServerResponse> createBook(ServerRequest request) {
        return request.formData()
                .flatMap(formData -> {
                    var formDto = new BookFormDto();
                    formDto.setTitle(formData.getFirst("title"));
                    formDto.setAuthorId(formData.getFirst("authorId"));
                    formDto.setGenreIds(new HashSet<>(formData.get("genreIds")));
                    
                    var createDto = new BookCreateDto(formDto.getTitle(), formDto.getAuthorId(), formDto.getGenreIds());
                    return bookService.create(createDto)
                            .flatMap(createdBook -> ServerResponse.seeOther(URI.create("/")).build())
                            .onErrorResume(EntityNotFoundException.class, e -> 
                                    handleBookFormError(e.getMessage(), formDto, null))
                            .onErrorResume(IllegalArgumentException.class, e -> 
                                    handleBookFormError(e.getMessage(), formDto, null));
                });
    }

    public Mono<ServerResponse> updateBook(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.formData()
                .flatMap(formData -> {
                    var formDto = new BookFormDto();
                    formDto.setTitle(formData.getFirst("title"));
                    formDto.setAuthorId(formData.getFirst("authorId"));
                    formDto.setGenreIds(new HashSet<>(formData.get("genreIds")));
                    
                    var updateDto = new BookUpdateDto(id, formDto.getTitle(), 
                            formDto.getAuthorId(), formDto.getGenreIds());
                    return bookService.update(updateDto)
                            .flatMap(updatedBook -> ServerResponse.seeOther(URI.create("/books/" + id)).build())
                            .onErrorResume(EntityNotFoundException.class, e -> 
                                    handleBookFormError(e.getMessage(), formDto, id))
                            .onErrorResume(IllegalArgumentException.class, e -> 
                                    handleBookFormError(e.getMessage(), formDto, id));
                });
    }

    private Mono<ServerResponse> handleBookFormError(String errorMessage, BookFormDto formDto, String bookId) {
        var bookDto = new BookDto(bookId, formDto.getTitle(), null, null);
        
        return Mono.zip(authorService.findAll().collectList(), genreService.findAll().collectList())
                .flatMap(tuple -> {
                    var model = createErrorModel(errorMessage, bookDto, bookId, tuple.getT1(), tuple.getT2());
                    return ServerResponse.ok()
                            .contentType(MediaType.TEXT_HTML)
                            .render("book/form", model);
                });
    }

    private Map<String, Object> createErrorModel(String errorMessage, BookDto bookDto, String bookId, 
                                                Object authors, Object genres) {
        if (bookId != null) {
            return Map.of(
                    "error", errorMessage,
                    "bookId", bookId,
                    "book", bookDto,
                    "authors", authors,
                    "genres", genres
            );
        }
        return Map.of(
                "error", errorMessage,
                "book", bookDto,
                "authors", authors,
                "genres", genres
        );
    }
}