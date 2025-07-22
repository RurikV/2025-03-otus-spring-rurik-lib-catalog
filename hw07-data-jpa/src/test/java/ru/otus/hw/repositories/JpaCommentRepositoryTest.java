package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Spring Data JPA для работы с комментариями ")
@DataJpaTest
class JpaCommentRepositoryTest {

    @Autowired
    private CommentRepository repository;

    @Autowired
    private TestEntityManager em;

    private Book book;
    private Comment comment;

    @BeforeEach
    void setUp() {
        book = em.find(Book.class, 1L);
        comment = em.find(Comment.class, 1L);
    }

    @DisplayName("должен загружать комментарий по id")
    @Test
    void shouldReturnCorrectCommentById() {
        var actualComment = repository.findById(comment.getId());
        assertThat(actualComment).isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(comment);
    }

    @DisplayName("должен загружать список комментариев по id книги")
    @Test
    void shouldReturnCorrectCommentsListByBookId() {
        var actualComments = repository.findAllByBookId(book.getId());
        assertThat(actualComments).isNotNull().hasSize(2)
                .allMatch(c -> c.getText() != null)
                .allMatch(c -> c.getBook() != null);
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldSaveNewComment() {
        // Arrange
        var expectedComment = new Comment(0, "New Comment", book);

        // Act
        var returnedComment = repository.save(expectedComment);
        var foundComment = repository.findById(returnedComment.getId());

        // Assert
        assertThat(returnedComment).isNotNull()
                .matches(c -> c.getId() > 0)
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedComment);

        assertThat(foundComment)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(returnedComment);
    }

    @DisplayName("должен сохранять измененный комментарий")
    @Test
    void shouldSaveUpdatedComment() {
        // Arrange
        var expectedComment = new Comment(comment.getId(), "Updated Comment", book);

        // Act
        var originalComment = repository.findById(expectedComment.getId());
        var returnedComment = repository.save(expectedComment);
        var foundComment = repository.findById(returnedComment.getId());

        // Assert
        assertThat(originalComment)
                .isPresent()
                .get()
                .isNotEqualTo(expectedComment);

        assertThat(returnedComment).isNotNull()
                .matches(c -> c.getId() > 0)
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedComment);

        assertThat(foundComment)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(returnedComment);
    }

    @DisplayName("должен удалять комментарий по id ")
    @Test
    void shouldDeleteComment() {
        // Arrange
        long commentId = comment.getId();

        // Act
        var commentBeforeDelete = repository.findById(commentId);
        repository.deleteById(commentId);
        em.flush();
        var commentAfterDelete = repository.findById(commentId);

        // Assert
        assertThat(commentBeforeDelete).isPresent();
        assertThat(commentAfterDelete).isEmpty();
    }
}
