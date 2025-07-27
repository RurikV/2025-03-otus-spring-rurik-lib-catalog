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
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BookPageController {

    private final AuthorService authorService;
    
    private final GenreService genreService;
    
    private final BookService bookService;
    
    private final CommentService commentService;

    @GetMapping("/")
    public ResponseEntity<List<BookDto>> listBooks() {
        return ResponseEntity.ok(bookService.findAll());
    }

    @GetMapping("/books")
    public ResponseEntity<List<BookDto>> listBooksAlternative() {
        return ResponseEntity.ok(bookService.findAll());
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<Map<String, Object>> viewBook(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        response.put("book", bookService.findById(id));
        response.put("bookId", id);
        response.put("comments", commentService.findByBookId(id));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/books/new")
    public ResponseEntity<Map<String, Object>> newBookForm() {
        Map<String, Object> response = new HashMap<>();
        response.put("book", new BookDto());
        response.put("authors", authorService.findAll());
        response.put("genres", genreService.findAll());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/books/{id}/edit")
    public ResponseEntity<Map<String, Object>> editBookForm(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        response.put("book", bookService.findById(id));
        response.put("authors", authorService.findAll());
        response.put("genres", genreService.findAll());
        response.put("bookId", id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/books/{id}/delete")
    public ResponseEntity<Map<String, Object>> deleteBookConfirm(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        response.put("bookId", id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/books")
    public ResponseEntity<BookDto> createBook(@RequestBody BookCreateDto createDto) {
        BookDto createdBook = bookService.create(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<BookDto> updateBook(@PathVariable String id, 
                                            @RequestBody BookUpdateDto updateDto) {
        updateDto.setId(id);
        BookDto updatedBook = bookService.update(updateDto);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}