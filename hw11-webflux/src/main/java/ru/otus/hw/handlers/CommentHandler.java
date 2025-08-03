package ru.otus.hw.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentFormDto;
import ru.otus.hw.dto.CommentUpdateDto;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;

import java.net.URI;
import java.util.Map;

import static ru.otus.hw.handlers.ErrorHandlingUtils.handleApiErrors;
import static ru.otus.hw.handlers.ErrorHandlingUtils.handlePageErrors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentHandler {

    private final CommentService commentService;

    private final BookService bookService;

    public Mono<ServerResponse> newCommentForm(ServerRequest request) {
        String bookId = request.pathVariable("bookId");
        return bookService.findById(bookId)
                .flatMap(book -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .render("comment/form", Map.of("book", book)))
                .switchIfEmpty(ServerResponse.seeOther(URI.create("/")).build())
                .onErrorResume(handlePageErrors("/"));
    }

    public Mono<ServerResponse> saveComment(ServerRequest request) {
        String bookId = request.pathVariable("bookId");
        
        return request.formData()
                .flatMap(formData -> {
                    var createDto = new CommentCreateDto(formData.getFirst("text"), bookId);
                    
                    return commentService.create(createDto)
                            .flatMap(createdComment -> 
                                    ServerResponse.seeOther(URI.create("/books/" + bookId)).build())
                            .onErrorResume(error -> 
                                    bookService.findById(bookId)
                                            .flatMap(book -> ServerResponse.ok()
                                                    .contentType(MediaType.TEXT_HTML)
                                                    .render("comment/form", 
                                                            Map.of("book", book, "error", error.getMessage())))
                                            .switchIfEmpty(ServerResponse.seeOther(URI.create("/")).build()));
                });
    }

    public Mono<ServerResponse> editCommentForm(ServerRequest request) {
        String id = request.pathVariable("id");
        return commentService.findById(id)
                .flatMap(comment -> 
                    bookService.findById(comment.getBookId())
                            .flatMap(book -> ServerResponse.ok()
                                    .contentType(MediaType.TEXT_HTML)
                                    .render("comment/edit", Map.of("comment", comment, "book", book)))
                            .switchIfEmpty(ServerResponse.ok()
                                    .contentType(MediaType.TEXT_HTML)
                                    .render("comment/edit", Map.of("comment", comment)))
                )
                .switchIfEmpty(ServerResponse.seeOther(URI.create("/")).build())
                .onErrorResume(handlePageErrors("/"));
    }

    public Mono<ServerResponse> updateComment(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.formData()
                .flatMap(formData -> {
                    var formDto = new CommentFormDto();
                    formDto.setText(formData.getFirst("text"));
                    
                    return commentService.findById(id)
                            .flatMap(comment -> {
                                var updateDto = new CommentUpdateDto(id, formDto.getText());
                                return commentService.update(updateDto)
                                        .flatMap(updatedComment -> 
                                                ServerResponse.seeOther(
                                                        URI.create("/books/" + comment.getBookId())).build());
                            })
                            .switchIfEmpty(ServerResponse.seeOther(URI.create("/")).build())
                            .onErrorResume(handlePageErrors("/"));
                });
    }

    public Mono<ServerResponse> deleteCommentConfirm(ServerRequest request) {
        String id = request.pathVariable("id");
        return commentService.findById(id)
                .flatMap(comment -> 
                    bookService.findById(comment.getBookId())
                            .flatMap(book -> ServerResponse.ok()
                                    .contentType(MediaType.TEXT_HTML)
                                    .render("comment/delete", Map.of("comment", comment, "book", book)))
                            .switchIfEmpty(ServerResponse.ok()
                                    .contentType(MediaType.TEXT_HTML)
                                    .render("comment/delete", Map.of("comment", comment)))
                )
                .switchIfEmpty(ServerResponse.seeOther(URI.create("/")).build())
                .onErrorResume(handlePageErrors("/"));
    }

    public Mono<ServerResponse> deleteComment(ServerRequest request) {
        String id = request.pathVariable("id");
        return commentService.findById(id)
                .flatMap(comment -> {
                    String bookId = comment.getBookId();
                    return commentService.deleteById(id)
                            .then(ServerResponse.seeOther(URI.create("/books/" + bookId)).build());
                })
                .switchIfEmpty(ServerResponse.seeOther(URI.create("/")).build())
                .onErrorResume(handlePageErrors("/"));
    }

    public Mono<ServerResponse> getCommentsByBookId(ServerRequest request) {
        String bookId = request.pathVariable("bookId");
        log.info("[DEBUG_LOG] CommentHandler.getCommentsByBookId() API called for bookId: {}", bookId);
        
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(commentService.findByBookId(bookId), ru.otus.hw.dto.CommentDto.class);
    }

    // AJAX API methods
    public Mono<ServerResponse> createComment(ServerRequest request) {
        String bookId = request.pathVariable("bookId");
        return request.bodyToMono(CommentCreateDto.class)
                .doOnNext(dto -> dto.setBookId(bookId))
                .flatMap(commentService::create)
                .flatMap(createdComment -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(createdComment))
                .onErrorResume(handleApiErrors());
    }

    public Mono<ServerResponse> updateCommentApi(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.bodyToMono(CommentUpdateDto.class)
                .doOnNext(dto -> dto.setId(id))
                .flatMap(commentService::update)
                .flatMap(updatedComment -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(updatedComment))
                .onErrorResume(handleApiErrors());
    }

    public Mono<ServerResponse> deleteCommentApi(ServerRequest request) {
        String id = request.pathVariable("id");
        return commentService.deleteById(id)
                .then(ServerResponse.noContent().build())
                .onErrorResume(handleApiErrors());
    }
}