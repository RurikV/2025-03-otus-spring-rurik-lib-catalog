package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
    public Comment insert(String text, String bookId) {
        if (!hasText(text)) {
            throw new IllegalArgumentException("Comment text must not be null or empty");
        }
        if (!hasText(bookId)) {
            throw new IllegalArgumentException("Book id must not be null or empty");
        }
        
        return save(null, text, bookId);
    }

    @Override
    public Comment update(String id, String text) {
        if (!hasText(id)) {
            throw new IllegalArgumentException("Comment id must not be null or empty");
        }
        if (!hasText(text)) {
            throw new IllegalArgumentException("Comment text must not be null or empty");
        }
        
        var comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %s not found".formatted(id)));
        return save(id, text, comment.getBook().getId());
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