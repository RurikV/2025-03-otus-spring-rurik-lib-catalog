package ru.otus.hw.batch.processors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.mappers.EntityMapper;
import ru.otus.hw.models.jpa.Author;
import ru.otus.hw.models.jpa.Book;
import ru.otus.hw.models.jpa.Comment;
import ru.otus.hw.models.jpa.Genre;
import ru.otus.hw.models.mongo.MongoAuthor;
import ru.otus.hw.models.mongo.MongoBook;
import ru.otus.hw.models.mongo.MongoComment;
import ru.otus.hw.models.mongo.MongoGenre;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchProcessorsTest {

    @Mock
    private EntityMapper entityMapper;

    private BookItemProcessor bookItemProcessor;
    private CommentItemProcessor commentItemProcessor;

    @BeforeEach
    void setUp() {
        bookItemProcessor = new BookItemProcessor(entityMapper);
        commentItemProcessor = new CommentItemProcessor(entityMapper);
    }

    @Test
    void bookProcessorShouldTransformMongoBookToJpaBook() throws Exception {
        System.out.println("[DEBUG_LOG] Testing BookItemProcessor transformation");
        
        // Given
        MongoAuthor mongoAuthor = new MongoAuthor("mongo-author-1", "Leo Tolstoy");
        MongoGenre mongoGenre = new MongoGenre("mongo-genre-1", "Classic Literature");
        MongoBook mongoBook = new MongoBook("mongo-book-1", "War and Peace", mongoAuthor, Arrays.asList(mongoGenre));
        
        Author jpaAuthor = new Author();
        jpaAuthor.setId(1L);
        jpaAuthor.setFullName("Leo Tolstoy");
        
        Genre jpaGenre = new Genre();
        jpaGenre.setId(1L);
        jpaGenre.setName("Classic Literature");
        
        Book expectedBook = new Book();
        expectedBook.setId(1L);
        expectedBook.setTitle("War and Peace");
        expectedBook.setAuthor(jpaAuthor);
        expectedBook.setGenres(Arrays.asList(jpaGenre));
        
        when(entityMapper.mapToBook(mongoBook)).thenReturn(expectedBook);

        // When
        Book result = bookItemProcessor.process(mongoBook);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("War and Peace");
        assertThat(result.getAuthor().getFullName()).isEqualTo("Leo Tolstoy");
        assertThat(result.getGenres()).hasSize(1);
        assertThat(result.getGenres().get(0).getName()).isEqualTo("Classic Literature");
    }

    @Test
    void bookProcessorShouldReturnNullForNullInput() throws Exception {
        System.out.println("[DEBUG_LOG] Testing BookItemProcessor with null input");
        
        // When
        Book result = bookItemProcessor.process(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void commentProcessorShouldTransformMongoCommentToJpaComment() {
        System.out.println("[DEBUG_LOG] Testing CommentItemProcessor transformation");
        
        // Given
        MongoBook mongoBook = new MongoBook("mongo-book-1", "War and Peace", null, null);
        MongoComment mongoComment = new MongoComment("mongo-comment-1", "Excellent masterpiece", mongoBook);
        
        Book jpaBook = new Book();
        jpaBook.setId(1L);
        jpaBook.setTitle("War and Peace");
        
        Comment expectedComment = new Comment();
        expectedComment.setText("Excellent masterpiece");
        expectedComment.setBook(jpaBook);
        
        when(entityMapper.mapToComment(mongoComment)).thenReturn(expectedComment);

        // When
        Comment result = commentItemProcessor.process(mongoComment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Excellent masterpiece");
        assertThat(result.getBook()).isNotNull();
        assertThat(result.getBook().getId()).isEqualTo(1L);
        assertThat(result.getBook().getTitle()).isEqualTo("War and Peace");
    }

    @Test
    void commentProcessorShouldHandleCommentWithNullBook() {
        System.out.println("[DEBUG_LOG] Testing CommentItemProcessor with null book");
        
        // Given
        MongoComment mongoComment = new MongoComment("mongo-comment-1", "Orphaned comment", null);
        
        Comment expectedComment = new Comment();
        expectedComment.setText("Orphaned comment");
        expectedComment.setBook(null);
        
        when(entityMapper.mapToComment(mongoComment)).thenReturn(expectedComment);

        // When
        Comment result = commentItemProcessor.process(mongoComment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Orphaned comment");
        assertThat(result.getBook()).isNull();
    }

    @Test
    void bookProcessorShouldHandleBookWithNullAuthor() throws Exception {
        System.out.println("[DEBUG_LOG] Testing BookItemProcessor with null author");
        
        // Given
        MongoBook mongoBook = new MongoBook("mongo-book-1", "Anonymous Book", null, null);
        
        Book expectedBook = new Book();
        expectedBook.setId(1L);
        expectedBook.setTitle("Anonymous Book");
        expectedBook.setAuthor(null);
        expectedBook.setGenres(null);
        
        when(entityMapper.mapToBook(mongoBook)).thenReturn(expectedBook);

        // When
        Book result = bookItemProcessor.process(mongoBook);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Anonymous Book");
        assertThat(result.getAuthor()).isNull();
        assertThat(result.getGenres()).isNull();
    }

    @Test
    void bookProcessorShouldHandleBookWithEmptyGenres() throws Exception {
        System.out.println("[DEBUG_LOG] Testing BookItemProcessor with empty genres");
        
        // Given
        MongoAuthor mongoAuthor = new MongoAuthor("mongo-author-1", "Unknown Author");
        MongoBook mongoBook = new MongoBook("mongo-book-1", "Book Without Genres", mongoAuthor, Arrays.asList());
        
        Author jpaAuthor = new Author();
        jpaAuthor.setId(1L);
        jpaAuthor.setFullName("Unknown Author");
        
        Book expectedBook = new Book();
        expectedBook.setId(1L);
        expectedBook.setTitle("Book Without Genres");
        expectedBook.setAuthor(jpaAuthor);
        expectedBook.setGenres(Arrays.asList());
        
        when(entityMapper.mapToBook(mongoBook)).thenReturn(expectedBook);

        // When
        Book result = bookItemProcessor.process(mongoBook);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Book Without Genres");
        assertThat(result.getAuthor().getFullName()).isEqualTo("Unknown Author");
        assertThat(result.getGenres()).isEmpty();
    }

    @Test
    void commentProcessorShouldHandleEmptyCommentText() {
        System.out.println("[DEBUG_LOG] Testing CommentItemProcessor with empty text");
        
        // Given
        MongoBook mongoBook = new MongoBook("mongo-book-1", "Some Book", null, null);
        MongoComment mongoComment = new MongoComment("mongo-comment-1", "", mongoBook);
        
        Book jpaBook = new Book();
        jpaBook.setId(1L);
        
        Comment expectedComment = new Comment();
        expectedComment.setText("");
        expectedComment.setBook(jpaBook);
        
        when(entityMapper.mapToComment(mongoComment)).thenReturn(expectedComment);

        // When
        Comment result = commentItemProcessor.process(mongoComment);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getText()).isEmpty();
        assertThat(result.getBook()).isNotNull();
        assertThat(result.getBook().getId()).isEqualTo(1L);
    }
}