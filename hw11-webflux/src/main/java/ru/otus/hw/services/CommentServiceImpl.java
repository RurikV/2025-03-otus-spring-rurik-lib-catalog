package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentDto;
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
    public Mono<CommentDto> findById(String id) {
        if (!hasText(id)) {
            return Mono.error(new IllegalArgumentException("Comment id must not be null or empty"));
        }
        return commentRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Comment with id %s not found".formatted(id))))
                .map(this::toCommentDto);
    }

    @Override
    public Flux<CommentDto> findByBookId(String bookId) {
        if (!hasText(bookId)) {
            return Flux.error(new IllegalArgumentException("Book id must not be null or empty"));
        }
        return commentRepository.findByBookId(bookId)
                .map(this::toCommentDto);
    }

    @Override
    public Mono<CommentDto> create(CommentCreateDto commentCreateDto) {
        if (!hasText(commentCreateDto.getText())) {
            return Mono.error(new IllegalArgumentException("Comment text must not be null or empty"));
        }
        if (!hasText(commentCreateDto.getBookId())) {
            return Mono.error(new IllegalArgumentException("Book id must not be null or empty"));
        }
        
        return save(null, commentCreateDto.getText(), commentCreateDto.getBookId())
                .map(this::toCommentDto);
    }

    @Override
    public Mono<CommentDto> update(CommentUpdateDto commentUpdateDto) {
        if (!hasText(commentUpdateDto.getId())) {
            return Mono.error(new IllegalArgumentException("Comment id must not be null or empty"));
        }
        if (!hasText(commentUpdateDto.getText())) {
            return Mono.error(new IllegalArgumentException("Comment text must not be null or empty"));
        }
        
        return commentRepository.findById(commentUpdateDto.getId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Comment with id %s not found".formatted(commentUpdateDto.getId()))))
                .flatMap(comment -> save(commentUpdateDto.getId(), commentUpdateDto.getText(), comment.getBookId()))
                .map(this::toCommentDto);
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
                    return commentRepository.save(comment);
                });
    }

    private CommentDto toCommentDto(Comment comment) {
        return new CommentDto(comment.getId(), comment.getText(), comment.getBookId());
    }
}