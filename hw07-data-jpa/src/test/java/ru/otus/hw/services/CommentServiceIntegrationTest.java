package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Интеграционные тесты сервиса комментариев")
@DataJpaTest
@Import({BookServiceImpl.class, CommentServiceImpl.class})
class CommentServiceIntegrationTest {

    @Autowired
    private CommentService commentService;

    @DisplayName("должен возвращать комментарий по id с доступными связями без LazyInitializationException")
    @Test
    void shouldReturnCommentByIdWithAccessibleRelationships() {
        // Arrange
        long commentId = 1L;

        // Act
        var actualComment = commentService.findById(commentId);

        // Assert
        assertThat(actualComment).isPresent();
        var comment = actualComment.get();

        // Проверяем, что можем получить доступ к связям без LazyInitializationException
        var book = comment.getBook();
        var author = book.getAuthor();
        var genres = book.getGenres();

        // Assert
        assertThat(actualComment).isPresent();
        assertThat(comment).isNotNull();

        assertThat(book).isNotNull();
        assertThat(book.getTitle()).isEqualTo("BookTitle_1");

        assertThat(author).isNotNull();
        assertThat(author.getFullName()).isEqualTo("Author_1");

        assertThat(genres).isNotNull().hasSize(2);
        assertThat(genres.get(0).getName()).isEqualTo("Genre_1");
        assertThat(genres.get(1).getName()).isEqualTo("Genre_2");

        // Проверяем полное соответствие с ожидаемым объектом
        var expectedComment = new Comment(1L, "Comment_1 for BookTitle_1", 
                new Book(1L, "BookTitle_1", 
                        new Author(1L, "Author_1"),
                        List.of(new Genre(1L, "Genre_1"), new Genre(2L, "Genre_2")),
                        List.of()));

        assertThat(comment).usingRecursiveComparison()
                .ignoringFields("book.comments") // избегаем циклических ссылок
                .ignoringFieldsMatchingRegexes(".*hibernateLazyInitializer.*", ".*\\$\\$_hibernate_.*")
                .ignoringActualNullFields()
                .isEqualTo(expectedComment);
    }

    @DisplayName("должен возвращать все комментарии по id книги с доступными связями без LazyInitializationException")
    @Test
    void shouldReturnAllCommentsByBookIdWithAccessibleRelationships() {
        // Arrange
        long bookId = 1L;

        // Act
        var actualComments = commentService.findAllByBookId(bookId);

        // Проверяем каждый комментарий на доступность связей
        for (var comment : actualComments) {
            // Проверяем, что можем получить доступ к связям без LazyInitializationException
            assertThat(comment.getBook()).isNotNull();
            assertThat(comment.getBook().getAuthor()).isNotNull();
            assertThat(comment.getBook().getGenres()).isNotNull();

            // Проверяем доступ к именам жанров
            for (var genre : comment.getBook().getGenres()) {
                assertThat(genre.getName()).isNotNull();
            }
        }

        // Проверяем первый комментарий полностью
        var firstComment = actualComments.get(0);

        // Assert
        assertThat(actualComments).isNotNull().hasSize(2);

        // Проверяем каждый комментарий на доступность связей
        for (var comment : actualComments) {
            var book = comment.getBook();
            var author = book.getAuthor();
            var genres = book.getGenres();

            assertThat(book).isNotNull();
            assertThat(book.getTitle()).isNotNull();

            assertThat(author).isNotNull();
            assertThat(author.getFullName()).isNotNull();

            assertThat(genres).isNotNull().isNotEmpty();
            for (var genre : genres) {
                assertThat(genre.getName()).isNotNull();
            }
        }

        var expectedFirstComment = new Comment(1L, "Comment_1 for BookTitle_1", 
                new Book(1L, "BookTitle_1", 
                        new Author(1L, "Author_1"),
                        List.of(new Genre(1L, "Genre_1"), new Genre(2L, "Genre_2")),
                        List.of()));

        assertThat(firstComment).usingRecursiveComparison()
                .ignoringFields("book.comments") // избегаем циклических ссылок
                .ignoringFieldsMatchingRegexes(".*hibernateLazyInitializer.*", ".*\\$\\$_hibernate_.*")
                .ignoringActualNullFields()
                .isEqualTo(expectedFirstComment);
    }

    @DisplayName("должен создавать новый комментарий с доступными связями без LazyInitializationException")
    @Test
    @Transactional
    void shouldInsertCommentWithAccessibleRelationships() {
        // Arrange
        String text = "New Comment Text";
        long bookId = 1L;

        // Act
        var actualComment = commentService.insert(text, bookId);

        // Проверяем, что можем получить доступ к связям без LazyInitializationException
        var book = actualComment.getBook();
        var author = book.getAuthor();
        var genres = book.getGenres();

        // Assert
        assertThat(actualComment).isNotNull();
        assertThat(actualComment.getId()).isGreaterThan(0);
        assertThat(actualComment.getText()).isEqualTo(text);

        assertThat(book).isNotNull();
        assertThat(book.getTitle()).isEqualTo("BookTitle_1");

        assertThat(author).isNotNull();
        assertThat(author.getFullName()).isEqualTo("Author_1");

        assertThat(genres).isNotNull().hasSize(2);
        assertThat(genres).extracting("name").containsExactlyInAnyOrder("Genre_1", "Genre_2");

        // Проверяем полное соответствие с ожидаемым объектом
        var expectedComment = new Comment(actualComment.getId(), text, 
                new Book(1L, "BookTitle_1", 
                        new Author(1L, "Author_1"),
                        List.of(new Genre(1L, "Genre_1"), new Genre(2L, "Genre_2")),
                        List.of()));

        assertThat(actualComment).usingRecursiveComparison()
                .ignoringFields("book.comments") // избегаем циклических ссылок
                .isEqualTo(expectedComment);
    }

    @DisplayName("должен обновлять комментарий с доступными связями без LazyInitializationException")
    @Test
    @Transactional
    void shouldUpdateCommentWithAccessibleRelationships() {
        // Arrange
        long commentId = 1L;
        String newText = "Updated Comment Text";

        // Act
        var actualComment = commentService.update(commentId, newText);

        // Проверяем, что можем получить доступ к связям без LazyInitializationException
        var book = actualComment.getBook();
        var author = book.getAuthor();
        var genres = book.getGenres();

        // Assert
        assertThat(actualComment).isNotNull();
        assertThat(actualComment.getId()).isEqualTo(commentId);
        assertThat(actualComment.getText()).isEqualTo(newText);

        assertThat(book).isNotNull();
        assertThat(book.getTitle()).isEqualTo("BookTitle_1");

        assertThat(author).isNotNull();
        assertThat(author.getFullName()).isEqualTo("Author_1");

        assertThat(genres).isNotNull().hasSize(2);
        assertThat(genres).extracting("name").containsExactlyInAnyOrder("Genre_1", "Genre_2");

        // Проверяем полное соответствие с ожидаемым объектом
        var expectedComment = new Comment(commentId, newText, 
                new Book(1L, "BookTitle_1", 
                        new Author(1L, "Author_1"),
                        List.of(new Genre(1L, "Genre_1"), new Genre(2L, "Genre_2")),
                        List.of()));

        assertThat(actualComment).usingRecursiveComparison()
                .ignoringFields("book.comments") // избегаем циклических ссылок
                .ignoringFieldsMatchingRegexes(".*hibernateLazyInitializer.*", ".*\\$\\$_hibernate_.*")
                .ignoringActualNullFields()
                .isEqualTo(expectedComment);
    }
}
