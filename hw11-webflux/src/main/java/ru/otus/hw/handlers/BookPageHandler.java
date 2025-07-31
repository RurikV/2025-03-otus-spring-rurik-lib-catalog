package ru.otus.hw.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.BookService;

import java.net.URI;
import java.util.HashSet;

import static ru.otus.hw.handlers.ErrorHandlingUtils.handlePageErrors;

@Component
@RequiredArgsConstructor
public class BookPageHandler {

    private final BookService bookService;

    public Mono<ServerResponse> listBooks(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .render("book/list");
    }

    public Mono<ServerResponse> viewBook(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .render("book/view")
                .onErrorResume(handlePageErrors("/"));
    }

    public Mono<ServerResponse> newBookForm(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .render("book/form")
                .onErrorResume(handlePageErrors("/"));
    }

    public Mono<ServerResponse> editBookForm(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .render("book/form")
                .onErrorResume(handlePageErrors("/"));
    }

    public Mono<ServerResponse> deleteBookConfirm(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .render("book/delete");
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
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .render("book/form");
    }

}