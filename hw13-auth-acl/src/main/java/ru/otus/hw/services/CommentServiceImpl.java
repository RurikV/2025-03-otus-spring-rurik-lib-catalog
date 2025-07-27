package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.UserRepository;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    private final UserRepository userRepository;

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
        var currentUser = getCurrentUser();
        return save(null, commentCreateDto.getText(), commentCreateDto.getBookId(), currentUser);
    }

    @Override
    public Comment update(CommentUpdateDto commentUpdateDto) {
        if (!hasText(commentUpdateDto.getText())) {
            throw new IllegalArgumentException("Comment text must not be null or empty");
        }
        
        var comment = commentRepository.findById(commentUpdateDto.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Comment with id %s not found".formatted(commentUpdateDto.getId())));
        
        // Check authorization
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = isCommentOwner(commentUpdateDto.getId(), username);
        
        if (!isAdmin && !isOwner) {
            throw new AuthorizationDeniedException("Access denied", null);
        }
        
        // Update only the text using setter, preserving user and book
        comment.setText(commentUpdateDto.getText());
        return commentRepository.save(comment);
    }

    @Override
    public void deleteById(String id) {
        // Check authorization
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = isCommentOwner(id, username);
        
        if (!isAdmin && !isOwner) {
            throw new AuthorizationDeniedException("Access denied", null);
        }
        
        commentRepository.deleteById(id);
    }

    public boolean isCommentOwner(String commentId, String username) {
        if (!hasText(commentId)) {
            throw new IllegalArgumentException("Comment id must not be null or empty");
        }
        if (!hasText(username)) {
            return false;
        }
        return commentRepository.findById(commentId)
                .map(comment -> comment.getUser() != null && username.equals(comment.getUser().getUsername()))
                .orElse(false);
    }

    private ru.otus.hw.models.User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }

    private Comment save(String id, String text, String bookId, ru.otus.hw.models.User user) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %s not found".formatted(bookId)));
        var comment = new Comment(id, text, book, user);
        return commentRepository.save(comment);
    }
}