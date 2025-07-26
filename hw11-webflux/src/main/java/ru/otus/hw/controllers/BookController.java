package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    private final AuthorService authorService;

    private final GenreService genreService;

    private final CommentService commentService;

    @GetMapping("/books")
    public Mono<ResponseEntity<Flux<BookDto>>> getAllBooks() {
        Flux<BookDto> books = bookService.findAll();
        return Mono.just(ResponseEntity.ok(books));
    }

    @GetMapping("/books/{id}")
    public Mono<ResponseEntity<BookDto>> getBook(@PathVariable String id) {
        return bookService.findById(id)
                .map(book -> ResponseEntity.ok(book));
    }

    @PostMapping("/books")
    public Mono<ResponseEntity<BookDto>> createBook(@Valid @RequestBody BookCreateDto bookCreateDto) {
        return bookService.create(bookCreateDto)
                .map(createdBook -> ResponseEntity.status(HttpStatus.CREATED).body(createdBook));
    }

    @PutMapping("/books/{id}")
    public Mono<ResponseEntity<BookDto>> updateBook(@PathVariable String id, 
                                                  @Valid @RequestBody BookUpdateDto bookUpdateDto) {
        bookUpdateDto.setId(id);
        return bookService.update(bookUpdateDto)
                .map(updatedBook -> ResponseEntity.ok(updatedBook));
    }

    @DeleteMapping("/books/{id}")
    public Mono<ResponseEntity<Void>> deleteBook(@PathVariable String id) {
        return bookService.deleteById(id)
                .map(result -> ResponseEntity.noContent().<Void>build());
    }

}