package ru.otus.hw.repositories.r2dbc;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.r2dbc.BookEntity;

public interface BookR2dbcRepository {
    Flux<BookEntity> findAll();
    
    Mono<BookEntity> findById(Long id);
    
    Mono<BookEntity> save(BookEntity book);
    
    Mono<Void> deleteById(Long id);
    
    Flux<BookEntity> findAllWithAuthorsAndGenres();
    
    Mono<BookEntity> findByIdWithAuthorAndGenres(Long id);
}