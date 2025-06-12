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
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookRepository should")
@DataMongoTest
@Import({TestMongoConfig.class, EmbeddedMongoDisabler.class})
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
        // Arrange
        Author author = authorRepository.save(new Author(null, "Test Author"));
        Genre genre1 = genreRepository.save(new Genre(null, "Test Genre 1"));
        Genre genre2 = genreRepository.save(new Genre(null, "Test Genre 2"));
        Book expectedBook = new Book(null, "Test Book", author, List.of(genre1, genre2));

        // Act
        Book savedBook = bookRepository.save(expectedBook);
        Book retrievedBook = bookRepository.findById(savedBook.getId()).orElseThrow();

        // Assert
        assertThat(savedBook.getId()).isNotNull();
        expectedBook.setId(savedBook.getId());
        assertThat(savedBook).usingRecursiveComparison().isEqualTo(expectedBook);

        // Assert
        assertThat(retrievedBook).isNotNull();
        assertThat(retrievedBook).usingRecursiveComparison().isEqualTo(savedBook);
    }

    @DisplayName("find all books")
    @Test
    void shouldFindAllBooks() {
        // Arrange
        Author author = authorRepository.save(new Author(null, "Test Author"));
        Genre genre = genreRepository.save(new Genre(null, "Test Genre"));
        Book book1 = new Book(null, "Test Book 1", author, List.of(genre));
        Book book2 = new Book(null, "Test Book 2", author, List.of(genre));
        bookRepository.saveAll(List.of(book1, book2));

        // Act
        List<Book> books = bookRepository.findAll();
        // Set IDs for expected books to match the actual books
        List<Book> savedBooks = books.stream()
                .filter(b -> b.getTitle().equals(book1.getTitle()) || b.getTitle().equals(book2.getTitle()))
                .toList();
        if (savedBooks.size() >= 2) {
            book1.setId(savedBooks.get(0).getId());
            book2.setId(savedBooks.get(1).getId());
        }

        // Assert
        assertThat(books).isNotEmpty();
        assertThat(books.size()).isGreaterThanOrEqualTo(2);

        if (savedBooks.size() >= 2) {
            // Use recursive comparison to verify the books exist in the result
            assertThat(books)
                .usingRecursiveFieldByFieldElementComparator()
                .contains(book1, book2);
        }
    }

    @DisplayName("delete book by id")
    @Test
    void shouldDeleteBookById() {
        // Arrange
        Author author = authorRepository.save(new Author(null, "Test Author"));
        Genre genre = genreRepository.save(new Genre(null, "Test Genre"));
        Book book = new Book(null, "Test Book", author, List.of(genre));
        Book savedBook = bookRepository.save(book);

        // Act
        bookRepository.deleteById(savedBook.getId());

        // Assert
        assertThat(bookRepository.findById(savedBook.getId())).isEmpty();
    }
}
