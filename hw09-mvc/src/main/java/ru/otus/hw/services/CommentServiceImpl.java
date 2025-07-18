package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    @Override
    public Comment findById(String id) {
        if (!hasText(id)) {
            throw new IllegalArgumentException("Comment id must not be null or empty");
        }
        return commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %s not found".formatted(id)));
    }

    @Override
    public List<Comment> findByBookId(String bookId) {
        if (!hasText(bookId)) {
            throw new IllegalArgumentException("Book id must not be null or empty");
        }
        return commentRepository.findByBookId(bookId);
    }

    @Override
    public Comment create(CommentCreateDto commentCreateDto) {
        if (!hasText(commentCreateDto.getText())) {
            throw new IllegalArgumentException("Comment text must not be null or empty");
        }
        if (!hasText(commentCreateDto.getBookId())) {
            throw new IllegalArgumentException("Book id must not be null or empty");
        }
        
        return save(null, commentCreateDto.getText(), commentCreateDto.getBookId());
    }

    @Override
    public Comment update(CommentUpdateDto commentUpdateDto) {
        if (!hasText(commentUpdateDto.getId())) {
            throw new IllegalArgumentException("Comment id must not be null or empty");
        }
        if (!hasText(commentUpdateDto.getText())) {
            throw new IllegalArgumentException("Comment text must not be null or empty");
        }
        
        var comment = commentRepository.findById(commentUpdateDto.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Comment with id %s not found".formatted(commentUpdateDto.getId())));
        return save(commentUpdateDto.getId(), commentUpdateDto.getText(), comment.getBook().getId());
    }

    @Override
    public void deleteById(String id) {
        if (!hasText(id)) {
            throw new IllegalArgumentException("Comment id must not be null or empty");
        }
        commentRepository.deleteById(id);
    }

    private Comment save(String id, String text, String bookId) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %s not found".formatted(bookId)));
        var comment = new Comment(id, text, book);
        return commentRepository.save(comment);
    }
}