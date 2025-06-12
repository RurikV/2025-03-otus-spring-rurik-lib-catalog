package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.config.EmbeddedMongoDisabler;
import ru.otus.hw.config.TestMongoConfig;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CommentRepository should")
@DataMongoTest
@Import({TestMongoConfig.class, EmbeddedMongoDisabler.class})
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
        Author author = authorRepository.save(new Author(null, "Test Author"));
        Genre genre = genreRepository.save(new Genre(null, "Test Genre"));
        Book book = bookRepository.save(new Book(null, "Test Book", author, List.of(genre)));
        Comment expectedComment = new Comment(null, "Test Comment", book);

        // Act
        Comment savedComment = commentRepository.save(expectedComment);
        Comment retrievedComment = commentRepository.findById(savedComment.getId()).orElseThrow();

        // Assert
        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getText()).isEqualTo(expectedComment.getText());
        assertThat(savedComment.getBook().getId()).isEqualTo(book.getId());

        assertThat(retrievedComment).isNotNull();
        assertThat(retrievedComment.getId()).isEqualTo(savedComment.getId());
        assertThat(retrievedComment.getText()).isEqualTo(expectedComment.getText());
        assertThat(retrievedComment.getBook().getId()).isEqualTo(book.getId());
    }

    @DisplayName("find comments by book id")
    @Test
    void shouldFindCommentsByBookId() {
        // Arrange
        Author author = authorRepository.save(new Author(null, "Test Author"));
        Genre genre = genreRepository.save(new Genre(null, "Test Genre"));
        Book book = bookRepository.save(new Book(null, "Test Book", author, List.of(genre)));
        Comment comment1 = new Comment(null, "Test Comment 1", book);
        Comment comment2 = new Comment(null, "Test Comment 2", book);
        commentRepository.saveAll(List.of(comment1, comment2));

        // Act
        List<Comment> comments = commentRepository.findByBookId(book.getId());

        // Assert
        assertThat(comments).isNotEmpty();
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getBook().getId()).isEqualTo(book.getId());
        assertThat(comments.get(1).getBook().getId()).isEqualTo(book.getId());
    }

    @DisplayName("delete comment by id")
    @Test
    void shouldDeleteCommentById() {
        // Arrange
        Author author = authorRepository.save(new Author(null, "Test Author"));
        Genre genre = genreRepository.save(new Genre(null, "Test Genre"));
        Book book = bookRepository.save(new Book(null, "Test Book", author, List.of(genre)));
        Comment comment = new Comment(null, "Test Comment", book);
        Comment savedComment = commentRepository.save(comment);

        // Act
        commentRepository.deleteById(savedComment.getId());

        // Assert
        assertThat(commentRepository.findById(savedComment.getId())).isEmpty();
    }
}
