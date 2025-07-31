package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.config.ConditionalMongoTestConfig;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CommentRepository should")
@DataMongoTest
@Import({ConditionalMongoTestConfig.class})
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @DisplayName("save comment correctly")
    @Test
    void shouldSaveComment() {
        // Arrange
        Author author = authorRepository.save(new Author(null, "Test Author")).block();
        Genre genre = genreRepository.save(new Genre(null, "Test Genre")).block();
        Book book = bookRepository.save(new Book(null, "Test Book", author, List.of(genre))).block();
        Comment expectedComment = new Comment(null, "Test Comment", book.getId());

        // Act
        Comment savedComment = commentRepository.save(expectedComment).block();
        Comment retrievedComment = commentRepository.findById(savedComment.getId()).block();

        // Assert
        assertThat(savedComment.getId()).isNotNull();
        expectedComment.setId(savedComment.getId());
        assertThat(savedComment).usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(".*CGLIB.*")
                .isEqualTo(expectedComment);

        assertThat(retrievedComment).isNotNull();
        assertThat(retrievedComment).usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(".*CGLIB.*")
                .isEqualTo(savedComment);
    }

    @DisplayName("find comments by book id")
    @Test
    void shouldFindCommentsByBookId() {
        // Arrange
        Author author = authorRepository.save(new Author(null, "Test Author")).block();
        Genre genre = genreRepository.save(new Genre(null, "Test Genre")).block();
        Book book = bookRepository.save(new Book(null, "Test Book", author, List.of(genre))).block();
        Comment comment1 = new Comment(null, "Test Comment 1", book.getId());
        Comment comment2 = new Comment(null, "Test Comment 2", book.getId());
        commentRepository.saveAll(List.of(comment1, comment2)).blockLast();

        // Act
        List<Comment> comments = commentRepository.findByBookId(book.getId()).collectList().block();
        // Set IDs for expected comments to match the actual comments
        comment1.setId(comments.get(0).getId());
        comment2.setId(comments.get(1).getId());

        // Assert
        assertThat(comments).isNotEmpty();
        assertThat(comments).hasSize(2);

        // Use recursive comparison to verify all fields match
        assertThat(comments)
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("book.CGLIB$CALLBACK_0", "book.CGLIB$BOUND")
            .containsExactlyInAnyOrder(comment1, comment2);
    }

    @DisplayName("delete comment by id")
    @Test
    void shouldDeleteCommentById() {
        // Arrange
        Author author = authorRepository.save(new Author(null, "Test Author")).block();
        Genre genre = genreRepository.save(new Genre(null, "Test Genre")).block();
        Book book = bookRepository.save(new Book(null, "Test Book", author, List.of(genre))).block();
        Comment comment = new Comment(null, "Test Comment", book.getId());
        Comment savedComment = commentRepository.save(comment).block();

        // Act
        commentRepository.deleteById(savedComment.getId()).block();

        // Assert
        assertThat(commentRepository.findById(savedComment.getId()).block()).isNull();
    }
}
