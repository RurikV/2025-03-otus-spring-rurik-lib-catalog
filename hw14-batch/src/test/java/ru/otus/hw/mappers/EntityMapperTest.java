package ru.otus.hw.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.models.jpa.Author;
import ru.otus.hw.models.jpa.Book;
import ru.otus.hw.models.jpa.Comment;
import ru.otus.hw.models.jpa.Genre;
import ru.otus.hw.models.mongo.MongoAuthor;
import ru.otus.hw.models.mongo.MongoBook;
import ru.otus.hw.models.mongo.MongoComment;
import ru.otus.hw.models.mongo.MongoGenre;
import ru.otus.hw.services.IdMappingService;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntityMapperTest {

    @Mock
    private IdMappingService idMappingService;

    private EntityMapper entityMapper;

    @BeforeEach
    void setUp() {
        entityMapper = new EntityMapper(idMappingService);
    }

    @Test
    void shouldMapMongoAuthorToJpaAuthor() {
        System.out.println("[DEBUG_LOG] Testing MongoAuthor to JPA Author mapping");
        
        // Given
        MongoAuthor mongoAuthor = new MongoAuthor("mongo-author-1", "Leo Tolstoy");
        when(idMappingService.getOrCreateAuthorId("mongo-author-1")).thenReturn(1L);

        // When
        Author author = entityMapper.mapToAuthor(mongoAuthor);

        // Then
        assertThat(author).isNotNull();
        assertThat(author.getId()).isEqualTo(1L);
        assertThat(author.getFullName()).isEqualTo("Leo Tolstoy");
    }

    @Test
    void shouldReturnNullWhenMongoAuthorIsNull() {
        System.out.println("[DEBUG_LOG] Testing null MongoAuthor mapping");
        
        // When
        Author author = entityMapper.mapToAuthor(null);

        // Then
        assertThat(author).isNull();
    }

    @Test
    void shouldMapMongoGenreToJpaGenre() {
        System.out.println("[DEBUG_LOG] Testing MongoGenre to JPA Genre mapping");
        
        // Given
        MongoGenre mongoGenre = new MongoGenre("mongo-genre-1", "Classic Literature");
        when(idMappingService.getOrCreateGenreId("mongo-genre-1")).thenReturn(1L);

        // When
        Genre genre = entityMapper.mapToGenre(mongoGenre);

        // Then
        assertThat(genre).isNotNull();
        assertThat(genre.getId()).isEqualTo(1L);
        assertThat(genre.getName()).isEqualTo("Classic Literature");
    }

    @Test
    void shouldReturnNullWhenMongoGenreIsNull() {
        System.out.println("[DEBUG_LOG] Testing null MongoGenre mapping");
        
        // When
        Genre genre = entityMapper.mapToGenre(null);

        // Then
        assertThat(genre).isNull();
    }

    @Test
    void shouldMapMongoBookToJpaBook() {
        System.out.println("[DEBUG_LOG] Testing MongoBook to JPA Book mapping");
        
        // Given
        MongoAuthor mongoAuthor = new MongoAuthor("mongo-author-1", "Leo Tolstoy");
        MongoGenre mongoGenre1 = new MongoGenre("mongo-genre-1", "Classic Literature");
        MongoGenre mongoGenre2 = new MongoGenre("mongo-genre-2", "Drama");
        List<MongoGenre> mongoGenres = Arrays.asList(mongoGenre1, mongoGenre2);
        
        MongoBook mongoBook = new MongoBook("mongo-book-1", "War and Peace", mongoAuthor, mongoGenres);
        
        when(idMappingService.getOrCreateBookId("mongo-book-1")).thenReturn(1L);
        when(idMappingService.getOrCreateAuthorId("mongo-author-1")).thenReturn(1L);
        when(idMappingService.getOrCreateGenreId("mongo-genre-1")).thenReturn(1L);
        when(idMappingService.getOrCreateGenreId("mongo-genre-2")).thenReturn(2L);

        // When
        Book book = entityMapper.mapToBook(mongoBook);

        // Then
        assertThat(book).isNotNull();
        assertThat(book.getId()).isEqualTo(1L);
        assertThat(book.getTitle()).isEqualTo("War and Peace");
        assertThat(book.getAuthor()).isNotNull();
        assertThat(book.getAuthor().getId()).isEqualTo(1L);
        assertThat(book.getAuthor().getFullName()).isEqualTo("Leo Tolstoy");
        assertThat(book.getGenres()).hasSize(2);
        assertThat(book.getGenres().get(0).getId()).isEqualTo(1L);
        assertThat(book.getGenres().get(1).getId()).isEqualTo(2L);
    }

    @Test
    void shouldReturnNullWhenMongoBookIsNull() {
        System.out.println("[DEBUG_LOG] Testing null MongoBook mapping");
        
        // When
        Book book = entityMapper.mapToBook(null);

        // Then
        assertThat(book).isNull();
    }

    @Test
    void shouldMapMongoCommentToJpaComment() {
        System.out.println("[DEBUG_LOG] Testing MongoComment to JPA Comment mapping");
        
        // Given
        MongoBook mongoBook = new MongoBook("mongo-book-1", "War and Peace", null, null);
        MongoComment mongoComment = new MongoComment("mongo-comment-1", "Excellent masterpiece", mongoBook);
        
        when(idMappingService.getBookId("mongo-book-1")).thenReturn(1L);

        // When
        Comment comment = entityMapper.mapToComment(mongoComment);

        // Then
        assertThat(comment).isNotNull();
        assertThat(comment.getText()).isEqualTo("Excellent masterpiece");
        assertThat(comment.getBook()).isNotNull();
        assertThat(comment.getBook().getId()).isEqualTo(1L);
    }

    @Test
    void shouldReturnNullWhenMongoCommentIsNull() {
        System.out.println("[DEBUG_LOG] Testing null MongoComment mapping");
        
        // When
        Comment comment = entityMapper.mapToComment(null);

        // Then
        assertThat(comment).isNull();
    }

    @Test
    void shouldHandleMongoBookWithNullAuthor() {
        System.out.println("[DEBUG_LOG] Testing MongoBook with null author mapping");
        
        // Given
        MongoBook mongoBook = new MongoBook("mongo-book-1", "Anonymous Book", null, null);
        when(idMappingService.getOrCreateBookId("mongo-book-1")).thenReturn(1L);

        // When
        Book book = entityMapper.mapToBook(mongoBook);

        // Then
        assertThat(book).isNotNull();
        assertThat(book.getId()).isEqualTo(1L);
        assertThat(book.getTitle()).isEqualTo("Anonymous Book");
        assertThat(book.getAuthor()).isNull();
        assertThat(book.getGenres()).isNull();
    }

    @Test
    void shouldHandleMongoCommentWithNullBook() {
        System.out.println("[DEBUG_LOG] Testing MongoComment with null book mapping");
        
        // Given
        MongoComment mongoComment = new MongoComment("mongo-comment-1", "Orphaned comment", null);

        // When
        Comment comment = entityMapper.mapToComment(mongoComment);

        // Then
        assertThat(comment).isNotNull();
        assertThat(comment.getText()).isEqualTo("Orphaned comment");
        assertThat(comment.getBook()).isNull();
    }
}