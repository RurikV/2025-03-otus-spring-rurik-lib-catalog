package ru.otus.hw.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CommentUpdateDto;

public interface CommentService {
    Mono<CommentDto> findById(String id);

    Flux<CommentDto> findByBookId(String bookId);

    Mono<CommentDto> create(CommentCreateDto commentCreateDto);

    Mono<CommentDto> update(CommentUpdateDto commentUpdateDto);

    Mono<Void> deleteById(String id);
}