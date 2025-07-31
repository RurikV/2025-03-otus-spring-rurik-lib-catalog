package ru.otus.hw.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
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

import static ru.otus.hw.handlers.ErrorHandlingUtils.handlePageErrors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentHandler {

    private final CommentService commentService;

    private final BookService bookService;
    
    private final Validator validator;

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
        log.info("[DEBUG_LOG] CommentHandler.saveComment() called for bookId: {}", bookId);
        
        return request.formData()
                .doOnNext(formData -> log.info("[DEBUG_LOG] Form data received: {}", formData))
                .flatMap(formData -> {
                    var formDto = new CommentFormDto();
                    formDto.setText(formData.getFirst("text"));
                    log.info("[DEBUG_LOG] Comment text from form: '{}'", formDto.getText());
                    
                    var createDto = new CommentCreateDto(formDto.getText(), bookId);
                    log.info("[DEBUG_LOG] Created CommentCreateDto: text='{}', bookId='{}'", createDto.getText(), createDto.getBookId());
                    
                    return validateCommentCreateDto(createDto)
                            .doOnNext(dto -> log.info("[DEBUG_LOG] Validation passed for comment"))
                            .doOnError(error -> log.error("[DEBUG_LOG] Validation failed: {}", error.getMessage()))
                            .flatMap(commentService::create)
                            .doOnNext(createdComment -> log.info("[DEBUG_LOG] Comment created successfully: {}", createdComment))
                            .doOnError(error -> log.error("[DEBUG_LOG] Comment creation failed: {}", error.getMessage()))
                            .flatMap(createdComment -> {
                                log.info("[DEBUG_LOG] Redirecting to /books/{}", bookId);
                                return ServerResponse.seeOther(URI.create("/books/" + bookId)).build();
                            })
                            .doOnError(error -> log.error("[DEBUG_LOG] Error in saveComment: {}", error.getMessage(), error))
                            .onErrorResume(error -> {
                                log.error("[DEBUG_LOG] Handling error in saveComment, redirecting to comment form with error");
                                return bookService.findById(bookId)
                                        .flatMap(book -> ServerResponse.ok()
                                                .contentType(MediaType.TEXT_HTML)
                                                .render("comment/form", Map.of("book", book, "error", error.getMessage())))
                                        .switchIfEmpty(ServerResponse.seeOther(URI.create("/")).build());
                            });
                });
    }
    
    private Mono<CommentCreateDto> validateCommentCreateDto(CommentCreateDto dto) {
        var errors = new BeanPropertyBindingResult(dto, "commentCreateDto");
        validator.validate(dto, errors);
        
        if (errors.hasErrors()) {
            var errorMessage = errors.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("Validation failed");
            return Mono.error(new IllegalArgumentException(errorMessage));
        }
        
        return Mono.just(dto);
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
}