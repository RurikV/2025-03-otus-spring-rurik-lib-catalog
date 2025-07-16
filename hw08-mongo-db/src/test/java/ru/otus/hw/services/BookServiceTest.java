package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.otus.hw.listeners.BookCascadeDeleteListener;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookService should")
@DataMongoTest
@Import({BookServiceImpl.class, BookCascadeDeleteListener.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @DisplayName("delete book and cascade delete its comments")
    @Test
    void shouldDeleteBookAndCascadeDeleteComments() {
        // Arrange
        Author author = authorRepository.save(new Author(null, "Test Author"));
        Genre genre = genreRepository.save(new Genre(null, "Test Genre"));
        Book book = bookRepository.save(new Book(null, "Test Book", author, List.of(genre)));

        Comment comment1 = new Comment(null, "Test Comment 1", book);
        Comment comment2 = new Comment(null, "Test Comment 2", book);
        commentRepository.saveAll(List.of(comment1, comment2));

        // Verify comments exist before deletion
        List<Comment> commentsBeforeDelete = commentRepository.findByBookId(book.getId());
        assertThat(commentsBeforeDelete).hasSize(2);

        // Act
        bookService.deleteById(book.getId());

        // Assert
        // Verify book is deleted
        assertThat(bookRepository.findById(book.getId())).isEmpty();

        // Verify comments are cascade deleted
        List<Comment> commentsAfterDelete = commentRepository.findByBookId(book.getId());
        assertThat(commentsAfterDelete).isEmpty();
    }
}
