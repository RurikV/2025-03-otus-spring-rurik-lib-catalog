package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import ru.otus.hw.config.ConditionalMongoTestConfig;
import ru.otus.hw.listeners.BookDeleteListener;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookRepository should")
@DataMongoTest
@Import({ConditionalMongoTestConfig.class, BookDeleteListener.class})
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @BeforeEach
    void setUp() {
        // Clear all collections before each test to ensure test isolation
        reactiveMongoTemplate.getCollectionNames()
            .flatMap(collectionName -> reactiveMongoTemplate.dropCollection(collectionName))
            .blockLast(); // Block to wait for completion in test setup
    }

    @DisplayName("save book correctly")
    @Test
    void shouldSaveBook() {
        // Arrange
        Author author = authorRepository.save(new Author(null, "Test Author")).block();
        Genre genre1 = genreRepository.save(new Genre(null, "Test Genre 1")).block();
        Genre genre2 = genreRepository.save(new Genre(null, "Test Genre 2")).block();
        Book expectedBook = new Book(null, "Test Book", author, List.of(genre1, genre2));

        // Act
        Book savedBook = bookRepository.save(expectedBook).block();
        Book retrievedBook = bookRepository.findById(savedBook.getId()).block();

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
        Author author = authorRepository.save(new Author(null, "Test Author")).block();
        Genre genre = genreRepository.save(new Genre(null, "Test Genre")).block();
        Book book1 = new Book(null, "Test Book 1", author, List.of(genre));
        Book book2 = new Book(null, "Test Book 2", author, List.of(genre));
        List<Book> savedBooks = bookRepository.saveAll(List.of(book1, book2)).collectList().block();

        // Act
        List<Book> allBooks = bookRepository.findAll().collectList().block();

        // Assert
        assertThat(allBooks).isNotEmpty();
        assertThat(allBooks).hasSize(2);

        // Use recursive comparison to verify the saved books exist in the result
        assertThat(allBooks)
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrderElementsOf(savedBooks);
    }

    @DisplayName("delete book by id")
    @Test
    void shouldDeleteBookById() {
        // Arrange
        Author author = authorRepository.save(new Author(null, "Test Author")).block();
        Genre genre = genreRepository.save(new Genre(null, "Test Genre")).block();
        Book book = new Book(null, "Test Book", author, List.of(genre));
        Book savedBook = bookRepository.save(book).block();

        // Act
        bookRepository.deleteById(savedBook.getId()).block();

        // Assert
        assertThat(bookRepository.findById(savedBook.getId()).block()).isNull();
    }
}
