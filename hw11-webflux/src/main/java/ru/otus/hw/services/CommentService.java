package ru.otus.hw.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentUpdateDto;
import ru.otus.hw.models.Comment;

public interface CommentService {
    Mono<Comment> findById(String id);

    Flux<Comment> findByBookId(String bookId);

    Mono<Comment> create(CommentCreateDto commentCreateDto);

    Mono<Comment> update(CommentUpdateDto commentUpdateDto);

    Mono<Void> deleteById(String id);
}