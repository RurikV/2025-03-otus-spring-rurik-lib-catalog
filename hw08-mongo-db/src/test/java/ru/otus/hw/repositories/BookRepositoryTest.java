package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.config.TestMongoConfig;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookRepository should")
@DataMongoTest
@Import(TestMongoConfig.class)
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @DisplayName("save book correctly")
    @Test
    void shouldSaveBook() {
        // Create test data
        Author author = authorRepository.save(new Author(null, "Test Author"));
        Genre genre1 = genreRepository.save(new Genre(null, "Test Genre 1"));
        Genre genre2 = genreRepository.save(new Genre(null, "Test Genre 2"));

        // Create and save a new book
        Book expectedBook = new Book(null, "Test Book", author, List.of(genre1, genre2));
        Book savedBook = bookRepository.save(expectedBook);

        // Verify the book was saved correctly
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getTitle()).isEqualTo(expectedBook.getTitle());
        assertThat(savedBook.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(savedBook.getGenres()).hasSize(2);
        assertThat(savedBook.getGenres().get(0).getId()).isEqualTo(genre1.getId());
        assertThat(savedBook.getGenres().get(1).getId()).isEqualTo(genre2.getId());

        // Verify the book can be retrieved
        Book retrievedBook = bookRepository.findById(savedBook.getId()).orElseThrow();
        assertThat(retrievedBook).isNotNull();
        assertThat(retrievedBook.getId()).isEqualTo(savedBook.getId());
        assertThat(retrievedBook.getTitle()).isEqualTo(expectedBook.getTitle());
    }

    @DisplayName("find all books")
    @Test
    void shouldFindAllBooks() {
        // Create test data
        Author author = authorRepository.save(new Author(null, "Test Author"));
        Genre genre = genreRepository.save(new Genre(null, "Test Genre"));

        // Create and save books
        Book book1 = new Book(null, "Test Book 1", author, List.of(genre));
        Book book2 = new Book(null, "Test Book 2", author, List.of(genre));
        bookRepository.saveAll(List.of(book1, book2));

        // Verify all books can be retrieved
        List<Book> books = bookRepository.findAll();
        assertThat(books).isNotEmpty();
        assertThat(books.size()).isGreaterThanOrEqualTo(2);
    }

    @DisplayName("delete book by id")
    @Test
    void shouldDeleteBookById() {
        // Create test data
        Author author = authorRepository.save(new Author(null, "Test Author"));
        Genre genre = genreRepository.save(new Genre(null, "Test Genre"));

        // Create and save a book
        Book book = new Book(null, "Test Book", author, List.of(genre));
        Book savedBook = bookRepository.save(book);

        // Delete the book
        bookRepository.deleteById(savedBook.getId());

        // Verify the book was deleted
        assertThat(bookRepository.findById(savedBook.getId())).isEmpty();
    }
}
