package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;
import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

@SpringBootTest
@DisplayName("CommentService Database Persistence Test")
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommentRepository commentRepository;

    private String testBookId;

    @BeforeEach
    void setUp() {
        System.out.println("[DEBUG_LOG] Setting up test data...");
        
        // Clean up database
        commentRepository.deleteAll().block();
        bookRepository.deleteAll().block();
        authorRepository.deleteAll().block();
        genreRepository.deleteAll().block();

        // Create test data
        Author author = new Author(null, "Test Author");
        String authorId = authorRepository.save(author).block().getId();

        Genre genre = new Genre(null, "Test Genre");
        String genreId = genreRepository.save(genre).block().getId();

        Book book = new Book(null, "Test Book", 
                new Author(authorId, "Test Author"), 
                List.of(new Genre(genreId, "Test Genre")));
        testBookId = bookRepository.save(book).block().getId();

        System.out.println("[DEBUG_LOG] Test setup complete - BookId: " + testBookId);
    }

    @Test
    @DisplayName("should create comment and persist to database")
    void shouldCreateCommentAndPersistToDatabase() {
        String commentText = "This is a test comment for database persistence";
        CommentCreateDto createDto = new CommentCreateDto(commentText, testBookId);
        
        System.out.println("[DEBUG_LOG] Creating comment with text: '" + commentText + "' for bookId: " + testBookId);
        
        // Count comments before creation
        long commentsBefore = commentRepository.findByBookId(testBookId).count().block();
        System.out.println("[DEBUG_LOG] Comments before creation: " + commentsBefore);
        
        // Create comment via service
        StepVerifier.create(commentService.create(createDto))
                .expectNextMatches(commentDto -> {
                    System.out.println("[DEBUG_LOG] Service returned CommentDto: id=" + commentDto.getId() + 
                                     ", text='" + commentDto.getText() + "', bookId=" + commentDto.getBookId());
                    return commentDto.getText().equals(commentText) && 
                           commentDto.getBookId().equals(testBookId) &&
                           commentDto.getId() != null;
                })
                .verifyComplete();
        
        // Verify comment exists in database
        StepVerifier.create(commentRepository.findByBookId(testBookId))
                .expectNextMatches(comment -> {
                    System.out.println("[DEBUG_LOG] Found comment in database: id=" + comment.getId() + 
                                     ", text='" + comment.getText() + "', bookId=" + comment.getBookId());
                    return comment.getText().equals(commentText) && 
                           comment.getBookId().equals(testBookId);
                })
                .verifyComplete();
        
        // Count comments after creation
        long commentsAfter = commentRepository.findByBookId(testBookId).count().block();
        System.out.println("[DEBUG_LOG] Comments after creation: " + commentsAfter);
        
        // Verify count increased
        assert commentsAfter == commentsBefore + 1 : 
            "Expected comment count to increase from " + commentsBefore + " to " + (commentsBefore + 1) + 
            " but got " + commentsAfter;
    }

    @Test
    @DisplayName("should retrieve comments by book id")
    void shouldRetrieveCommentsByBookId() {
        String commentText1 = "First comment";
        String commentText2 = "Second comment";
        
        System.out.println("[DEBUG_LOG] Creating multiple comments for bookId: " + testBookId);
        
        // Create two comments
        commentService.create(new CommentCreateDto(commentText1, testBookId)).block();
        commentService.create(new CommentCreateDto(commentText2, testBookId)).block();
        
        // Retrieve comments
        StepVerifier.create(commentService.findByBookId(testBookId))
                .expectNextMatches(comment -> {
                    System.out.println("[DEBUG_LOG] Retrieved comment 1: " + comment.getText());
                    return comment.getText().equals(commentText1);
                })
                .expectNextMatches(comment -> {
                    System.out.println("[DEBUG_LOG] Retrieved comment 2: " + comment.getText());
                    return comment.getText().equals(commentText2);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should handle comment creation with validation errors")
    void shouldHandleCommentCreationWithValidationErrors() {
        System.out.println("[DEBUG_LOG] Testing comment creation with empty text");
        
        CommentCreateDto createDto = new CommentCreateDto("", testBookId);
        
        StepVerifier.create(commentService.create(createDto))
                .expectError(IllegalArgumentException.class)
                .verify();
        
        // Verify no comment was created
        StepVerifier.create(commentRepository.findByBookId(testBookId))
                .verifyComplete();
    }

    @Test
    @DisplayName("should handle comment creation for non-existent book")
    void shouldHandleCommentCreationForNonExistentBook() {
        String nonExistentBookId = "507f1f77bcf86cd799439011";
        System.out.println("[DEBUG_LOG] Testing comment creation for non-existent bookId: " + nonExistentBookId);
        
        CommentCreateDto createDto = new CommentCreateDto("Test comment", nonExistentBookId);
        
        StepVerifier.create(commentService.create(createDto))
                .expectError()
                .verify();
    }
}