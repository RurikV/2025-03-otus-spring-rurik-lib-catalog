package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.config.ConditionalMongoTestConfig;
import ru.otus.hw.listeners.BookDeleteListener;
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

@DisplayName("BookDeleteListener should")
@DataMongoTest
@Import({ConditionalMongoTestConfig.class, BookDeleteListener.class})
class BookServiceTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @DisplayName("automatically delete comments when book is deleted")
    @Test
    void shouldDeleteCommentsWhenBookIsDeleted() {
        // Arrange
        Author author = authorRepository.save(new Author(null, "Test Author")).block();
        Genre genre = genreRepository.save(new Genre(null, "Test Genre")).block();
        Book book = bookRepository.save(new Book(null, "Test Book", author, List.of(genre))).block();
        
        // Create comments for the book
        Comment comment1 = commentRepository.save(new Comment(null, "Comment 1", book.getId())).block();
        Comment comment2 = commentRepository.save(new Comment(null, "Comment 2", book.getId())).block();
        
        // Create another book with comments to ensure they are not affected
        Book anotherBook = bookRepository.save(new Book(null, "Another Book", author, List.of(genre))).block();
        Comment anotherComment = commentRepository.save(new Comment(null, "Another Comment", anotherBook.getId())).block();
        
        // Verify initial state
        assertThat(bookRepository.findById(book.getId()).block()).isNotNull();
        assertThat(commentRepository.findByBookId(book.getId()).collectList().block()).hasSize(2);
        assertThat(bookRepository.findById(anotherBook.getId()).block()).isNotNull();
        assertThat(commentRepository.findByBookId(anotherBook.getId()).collectList().block()).hasSize(1);

        // Act - delete book directly through repository (should trigger BookDeleteListener)
        bookRepository.deleteById(book.getId()).block();

        // Assert
        // Book should be deleted
        assertThat(bookRepository.findById(book.getId()).block()).isNull();
        
        // Comments for the deleted book should also be deleted by BookDeleteListener
        assertThat(commentRepository.findByBookId(book.getId()).collectList().block()).isEmpty();
        
        // Other book and its comments should remain unaffected
        assertThat(bookRepository.findById(anotherBook.getId()).block()).isNotNull();
        assertThat(commentRepository.findByBookId(anotherBook.getId()).collectList().block()).hasSize(1);
        assertThat(commentRepository.findById(anotherComment.getId()).block()).isNotNull();
    }
}