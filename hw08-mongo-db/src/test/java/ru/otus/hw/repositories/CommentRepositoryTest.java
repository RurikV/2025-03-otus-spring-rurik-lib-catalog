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
        // Create test data
        Author author = authorRepository.save(new Author(null, "Test Author"));
        Genre genre = genreRepository.save(new Genre(null, "Test Genre"));
        Book book = bookRepository.save(new Book(null, "Test Book", author, List.of(genre)));

        // Create and save a new comment
        Comment expectedComment = new Comment(null, "Test Comment", book);
        Comment savedComment = commentRepository.save(expectedComment);

        // Verify the comment was saved correctly
        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getText()).isEqualTo(expectedComment.getText());
        assertThat(savedComment.getBook().getId()).isEqualTo(book.getId());

        // Verify the comment can be retrieved
        Comment retrievedComment = commentRepository.findById(savedComment.getId()).orElseThrow();
        assertThat(retrievedComment).isNotNull();
        assertThat(retrievedComment.getId()).isEqualTo(savedComment.getId());
        assertThat(retrievedComment.getText()).isEqualTo(expectedComment.getText());
        assertThat(retrievedComment.getBook().getId()).isEqualTo(book.getId());
    }

    @DisplayName("find comments by book id")
    @Test
    void shouldFindCommentsByBookId() {
        // Create test data
        Author author = authorRepository.save(new Author(null, "Test Author"));
        Genre genre = genreRepository.save(new Genre(null, "Test Genre"));
        Book book = bookRepository.save(new Book(null, "Test Book", author, List.of(genre)));

        // Create and save comments
        Comment comment1 = new Comment(null, "Test Comment 1", book);
        Comment comment2 = new Comment(null, "Test Comment 2", book);
        commentRepository.saveAll(List.of(comment1, comment2));

        // Verify comments can be retrieved by book id
        List<Comment> comments = commentRepository.findByBookId(book.getId());
        assertThat(comments).isNotEmpty();
        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getBook().getId()).isEqualTo(book.getId());
        assertThat(comments.get(1).getBook().getId()).isEqualTo(book.getId());
    }

    @DisplayName("delete comment by id")
    @Test
    void shouldDeleteCommentById() {
        // Create test data
        Author author = authorRepository.save(new Author(null, "Test Author"));
        Genre genre = genreRepository.save(new Genre(null, "Test Genre"));
        Book book = bookRepository.save(new Book(null, "Test Book", author, List.of(genre)));

        // Create and save a comment
        Comment comment = new Comment(null, "Test Comment", book);
        Comment savedComment = commentRepository.save(comment);

        // Delete the comment
        commentRepository.deleteById(savedComment.getId());

        // Verify the comment was deleted
        assertThat(commentRepository.findById(savedComment.getId())).isEmpty();
    }
}