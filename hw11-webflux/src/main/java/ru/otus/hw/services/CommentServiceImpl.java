package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    @Override
    public Mono<Comment> findById(String id) {
        if (!hasText(id)) {
            return Mono.error(new IllegalArgumentException("Comment id must not be null or empty"));
        }
        return commentRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Comment with id %s not found".formatted(id))))
                .flatMap(this::populateBookField);
    }

    @Override
    public Flux<Comment> findByBookId(String bookId) {
        if (!hasText(bookId)) {
            return Flux.error(new IllegalArgumentException("Book id must not be null or empty"));
        }
        return commentRepository.findByBookId(bookId)
                .flatMap(this::populateBookField);
    }

    @Override
    public Mono<Comment> create(CommentCreateDto commentCreateDto) {
        if (!hasText(commentCreateDto.getText())) {
            return Mono.error(new IllegalArgumentException("Comment text must not be null or empty"));
        }
        if (!hasText(commentCreateDto.getBookId())) {
            return Mono.error(new IllegalArgumentException("Book id must not be null or empty"));
        }
        
        return save(null, commentCreateDto.getText(), commentCreateDto.getBookId());
    }

    @Override
    public Mono<Comment> update(CommentUpdateDto commentUpdateDto) {
        if (!hasText(commentUpdateDto.getId())) {
            return Mono.error(new IllegalArgumentException("Comment id must not be null or empty"));
        }
        if (!hasText(commentUpdateDto.getText())) {
            return Mono.error(new IllegalArgumentException("Comment text must not be null or empty"));
        }
        
        return commentRepository.findById(commentUpdateDto.getId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Comment with id %s not found".formatted(commentUpdateDto.getId()))))
                .flatMap(comment -> save(commentUpdateDto.getId(), commentUpdateDto.getText(), comment.getBookId()));
    }

    @Override
    public Mono<Void> deleteById(String id) {
        if (!hasText(id)) {
            return Mono.error(new IllegalArgumentException("Comment id must not be null or empty"));
        }
        return commentRepository.deleteById(id);
    }

    private Mono<Comment> save(String id, String text, String bookId) {
        return bookRepository.findById(bookId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Book with id %s not found".formatted(bookId))))
                .flatMap(book -> {
                    var comment = new Comment(id, text, book.getId());
                    comment.setBook(book); // Set the book field immediately
                    return commentRepository.save(comment);
                });
    }

    private Mono<Comment> populateBookField(Comment comment) {
        return bookRepository.findById(comment.getBookId())
                .map(book -> {
                    comment.setBook(book);
                    return comment;
                })
                .switchIfEmpty(Mono.just(comment)); // Return comment even if book not found
    }
}