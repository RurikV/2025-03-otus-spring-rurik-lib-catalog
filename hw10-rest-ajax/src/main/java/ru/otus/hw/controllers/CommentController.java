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
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentUpdateDto;
import ru.otus.hw.models.Comment;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    private final BookService bookService;

    @GetMapping("/books/{bookId}/comments/new")
    public ResponseEntity<BookDto> newCommentForm(@PathVariable String bookId) {
        BookDto book = bookService.findById(bookId);
        return ResponseEntity.ok(book);
    }

    @PostMapping("/books/{bookId}/comments")
    public ResponseEntity<Comment> saveComment(@PathVariable String bookId,
                                             @RequestBody CommentCreateDto createDto) {
        createDto.setBookId(bookId);
        Comment comment = commentService.create(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @GetMapping("/comments/{id}/edit")
    public ResponseEntity<Comment> editCommentForm(@PathVariable String id) {
        Comment comment = commentService.findById(id);
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<Comment> updateComment(@PathVariable String id,
                                               @RequestBody CommentUpdateDto updateDto) {
        updateDto.setId(id);
        Comment comment = commentService.update(updateDto);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/comments/{id}/delete")
    public ResponseEntity<Comment> deleteCommentConfirm(@PathVariable String id) {
        Comment comment = commentService.findById(id);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable String id) {
        commentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}