package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.JpaAuthorRepository;
import ru.otus.hw.repositories.JpaBookRepository;
import ru.otus.hw.repositories.JpaCommentRepository;
import ru.otus.hw.repositories.JpaGenreRepository;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Интеграционные тесты сервиса книг")
@DataJpaTest
@Import({BookServiceImpl.class, CommentServiceImpl.class, 
         JpaBookRepository.class, JpaCommentRepository.class, 
         JpaAuthorRepository.class, JpaGenreRepository.class})
class BookServiceIntegrationTest {

    @Autowired
    private BookService bookService;

    @DisplayName("должен возвращать книгу по id с доступными связями без LazyInitializationException")
    @Test
    void shouldReturnBookByIdWithAccessibleRelationships() {
        // Arrange
        long bookId = 1L;

        // Act
        var actualBook = bookService.findById(bookId);

        // Assert
        assertThat(actualBook).isPresent();
        var book = actualBook.get();

        // Проверяем, что можем получить доступ к связям без LazyInitializationException
        var author = book.getAuthor();
        var genres = book.getGenres();
        var comments = book.getComments();

        // Assert
        assertThat(actualBook).isPresent();
        assertThat(book).isNotNull();

        assertThat(author).isNotNull();
        assertThat(author.getFullName()).isEqualTo("Author_1");

        assertThat(genres).isNotNull().hasSize(2);
        assertThat(genres.get(0).getName()).isEqualTo("Genre_1");
        assertThat(genres.get(1).getName()).isEqualTo("Genre_2");

        assertThat(comments).isNotNull().hasSize(2);

        // Проверяем полное соответствие с ожидаемым объектом
        var expectedBook = new Book(1L, "BookTitle_1", 
                new Author(1L, "Author_1"),
                List.of(new Genre(1L, "Genre_1"), new Genre(2L, "Genre_2")),
                comments);

        assertThat(book).usingRecursiveComparison()
                .ignoringFields("comments.book") // избегаем циклических ссылок
                .isEqualTo(expectedBook);
    }

    @DisplayName("должен возвращать все книги с доступными связями без LazyInitializationException")
    @Test
    void shouldReturnAllBooksWithAccessibleRelationships() {
        // Arrange & Act
        var actualBooks = bookService.findAll();

        // Проверяем каждую книгу на доступность связей
        for (var book : actualBooks) {
            // Проверяем, что можем получить доступ к связям без LazyInitializationException
            assertThat(book.getAuthor()).isNotNull();
            assertThat(book.getGenres()).isNotNull();
            assertThat(book.getComments()).isNotNull();

            // Проверяем доступ к именам жанров
            for (var genre : book.getGenres()) {
                assertThat(genre.getName()).isNotNull();
            }
        }

        // Проверяем первую книгу полностью
        var firstBook = actualBooks.get(0);

        // Assert
        assertThat(actualBooks).isNotNull().hasSize(3);

        // Проверяем каждую книгу на доступность связей
        for (var book : actualBooks) {
            var author = book.getAuthor();
            var genres = book.getGenres();
            var comments = book.getComments();

            assertThat(author).isNotNull();
            assertThat(author.getFullName()).isNotNull();

            assertThat(genres).isNotNull().isNotEmpty();
            for (var genre : genres) {
                assertThat(genre.getName()).isNotNull();
            }

            assertThat(comments).isNotNull();
        }

        var expectedFirstBook = new Book(1L, "BookTitle_1", 
                new Author(1L, "Author_1"),
                List.of(new Genre(1L, "Genre_1"), new Genre(2L, "Genre_2")),
                firstBook.getComments());

        assertThat(firstBook).usingRecursiveComparison()
                .ignoringFields("comments.book") // избегаем циклических ссылок
                .isEqualTo(expectedFirstBook);
    }

    @DisplayName("должен создавать новую книгу с доступными связями без LazyInitializationException")
    @Test
    @Transactional
    void shouldInsertBookWithAccessibleRelationships() {
        // Arrange
        String title = "New Book Title";
        long authorId = 1L;
        Set<Long> genreIds = Set.of(1L, 3L);

        // Act
        var actualBook = bookService.insert(title, authorId, genreIds);

        // Проверяем, что можем получить доступ к связям без LazyInitializationException
        var author = actualBook.getAuthor();
        var genres = actualBook.getGenres();
        var comments = actualBook.getComments();

        // Assert
        assertThat(actualBook).isNotNull();
        assertThat(actualBook.getId()).isGreaterThan(0);

        assertThat(author).isNotNull();
        assertThat(author.getFullName()).isEqualTo("Author_1");

        assertThat(genres).isNotNull().hasSize(2);
        assertThat(genres).extracting("name").containsExactlyInAnyOrder("Genre_1", "Genre_3");

        assertThat(comments).isNotNull().isEmpty();

        // Проверяем полное соответствие с ожидаемым объектом
        var expectedBook = new Book(actualBook.getId(), title, 
                new Author(1L, "Author_1"),
                List.of(new Genre(1L, "Genre_1"), new Genre(3L, "Genre_3")),
                List.of());

        assertThat(actualBook).usingRecursiveComparison()
                .ignoringFieldsOfTypes(java.util.ArrayList.class) // игнорируем конкретный тип списка
                .isEqualTo(expectedBook);
    }

    @DisplayName("должен обновлять книгу с доступными связями без LazyInitializationException")
    @Test
    @Transactional
    void shouldUpdateBookWithAccessibleRelationships() {
        // Arrange
        long bookId = 1L;
        String newTitle = "Updated Book Title";
        long newAuthorId = 2L;
        Set<Long> newGenreIds = Set.of(3L, 4L);

        // Act
        var actualBook = bookService.update(bookId, newTitle, newAuthorId, newGenreIds);

        // Проверяем, что можем получить доступ к связям без LazyInitializationException
        var author = actualBook.getAuthor();
        var genres = actualBook.getGenres();
        var comments = actualBook.getComments();

        // Assert
        assertThat(actualBook).isNotNull();
        assertThat(actualBook.getId()).isEqualTo(bookId);

        assertThat(author).isNotNull();
        assertThat(author.getFullName()).isEqualTo("Author_2");

        assertThat(genres).isNotNull().hasSize(2);
        assertThat(genres).extracting("name").containsExactlyInAnyOrder("Genre_3", "Genre_4");

        assertThat(comments).isNotNull(); // комментарии сохраняются при обновлении

        // Проверяем полное соответствие с ожидаемым объектом
        var expectedBook = new Book(bookId, newTitle, 
                new Author(2L, "Author_2"),
                List.of(new Genre(3L, "Genre_3"), new Genre(4L, "Genre_4")),
                comments);

        assertThat(actualBook).usingRecursiveComparison()
                .ignoringFieldsOfTypes(java.util.ArrayList.class) // игнорируем конкретный тип списка
                .isEqualTo(expectedBook);
    }
}
