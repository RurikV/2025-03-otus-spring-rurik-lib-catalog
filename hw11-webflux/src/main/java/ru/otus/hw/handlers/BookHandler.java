package ru.otus.hw.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.services.BookService;

@Component
@RequiredArgsConstructor
public class BookHandler {

    private final BookService bookService;

    public Mono<ServerResponse> getAllBooks(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(bookService.findAll(), ru.otus.hw.dto.BookDto.class)
                .onErrorResume(Exception.class, 
                        e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    public Mono<ServerResponse> getBook(ServerRequest request) {
        String id = request.pathVariable("id");
        return bookService.findById(id)
                .flatMap(book -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(book))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> createBook(ServerRequest request) {
        return request.bodyToMono(BookCreateDto.class)
                .flatMap(bookService::create)
                .flatMap(createdBook -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(createdBook))
                .onErrorResume(IllegalArgumentException.class, 
                        e -> ServerResponse.badRequest().bodyValue(e.getMessage()))
                .onErrorResume(Exception.class, 
                        e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    public Mono<ServerResponse> updateBook(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.bodyToMono(BookUpdateDto.class)
                .doOnNext(dto -> dto.setId(id))
                .flatMap(bookService::update)
                .flatMap(updatedBook -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(updatedBook))
                .onErrorResume(IllegalArgumentException.class, 
                        e -> ServerResponse.badRequest().bodyValue(e.getMessage()))
                .onErrorResume(Exception.class, 
                        e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    public Mono<ServerResponse> deleteBook(ServerRequest request) {
        String id = request.pathVariable("id");
        return bookService.deleteById(id)
                .then(ServerResponse.noContent().build())
                .onErrorResume(Exception.class, 
                        e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}