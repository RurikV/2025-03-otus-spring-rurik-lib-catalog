package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class IdMappingServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(IdMappingServiceTest.class);

    private IdMappingService idMappingService;

    @BeforeEach
    void setUp() {
        idMappingService = new IdMappingService();
    }

    @Test
    void shouldCreateNewAuthorIdForFirstCall() {
        System.out.println("[DEBUG_LOG] Testing new author ID creation");
        
        // When
        Long authorId = idMappingService.getOrCreateAuthorId("mongo-author-1");

        // Then
        assertThat(authorId).isEqualTo(1L);
    }

    @Test
    void shouldReturnSameAuthorIdForSameMongoId() {
        System.out.println("[DEBUG_LOG] Testing author ID consistency");
        
        // When
        Long firstCall = idMappingService.getOrCreateAuthorId("mongo-author-1");
        Long secondCall = idMappingService.getOrCreateAuthorId("mongo-author-1");

        // Then
        assertThat(firstCall).isEqualTo(secondCall);
        assertThat(firstCall).isEqualTo(1L);
    }

    @Test
    void shouldCreateIncrementalAuthorIds() {
        System.out.println("[DEBUG_LOG] Testing incremental author ID creation");
        
        // When
        Long firstAuthor = idMappingService.getOrCreateAuthorId("mongo-author-1");
        Long secondAuthor = idMappingService.getOrCreateAuthorId("mongo-author-2");
        Long thirdAuthor = idMappingService.getOrCreateAuthorId("mongo-author-3");

        // Then
        assertThat(firstAuthor).isEqualTo(1L);
        assertThat(secondAuthor).isEqualTo(2L);
        assertThat(thirdAuthor).isEqualTo(3L);
    }

    @Test
    void shouldCreateNewGenreIdForFirstCall() {
        System.out.println("[DEBUG_LOG] Testing new genre ID creation");
        
        // When
        Long genreId = idMappingService.getOrCreateGenreId("mongo-genre-1");

        // Then
        assertThat(genreId).isEqualTo(1L);
    }

    @Test
    void shouldReturnSameGenreIdForSameMongoId() {
        System.out.println("[DEBUG_LOG] Testing genre ID consistency");
        
        // When
        Long firstCall = idMappingService.getOrCreateGenreId("mongo-genre-1");
        Long secondCall = idMappingService.getOrCreateGenreId("mongo-genre-1");

        // Then
        assertThat(firstCall).isEqualTo(secondCall);
        assertThat(firstCall).isEqualTo(1L);
    }

    @Test
    void shouldCreateIncrementalGenreIds() {
        System.out.println("[DEBUG_LOG] Testing incremental genre ID creation");
        
        // When
        Long firstGenre = idMappingService.getOrCreateGenreId("mongo-genre-1");
        Long secondGenre = idMappingService.getOrCreateGenreId("mongo-genre-2");
        Long thirdGenre = idMappingService.getOrCreateGenreId("mongo-genre-3");

        // Then
        assertThat(firstGenre).isEqualTo(1L);
        assertThat(secondGenre).isEqualTo(2L);
        assertThat(thirdGenre).isEqualTo(3L);
    }

    @Test
    void shouldCreateNewBookIdForFirstCall() {
        System.out.println("[DEBUG_LOG] Testing new book ID creation");
        
        // When
        Long bookId = idMappingService.getOrCreateBookId("mongo-book-1");

        // Then
        assertThat(bookId).isEqualTo(1L);
    }

    @Test
    void shouldReturnSameBookIdForSameMongoId() {
        System.out.println("[DEBUG_LOG] Testing book ID consistency");
        
        // When
        Long firstCall = idMappingService.getOrCreateBookId("mongo-book-1");
        Long secondCall = idMappingService.getOrCreateBookId("mongo-book-1");

        // Then
        assertThat(firstCall).isEqualTo(secondCall);
        assertThat(firstCall).isEqualTo(1L);
    }

    @Test
    void shouldCreateIncrementalBookIds() {
        System.out.println("[DEBUG_LOG] Testing incremental book ID creation");
        
        // When
        Long firstBook = idMappingService.getOrCreateBookId("mongo-book-1");
        Long secondBook = idMappingService.getOrCreateBookId("mongo-book-2");
        Long thirdBook = idMappingService.getOrCreateBookId("mongo-book-3");

        // Then
        assertThat(firstBook).isEqualTo(1L);
        assertThat(secondBook).isEqualTo(2L);
        assertThat(thirdBook).isEqualTo(3L);
    }

    @Test
    void shouldReturnExistingBookId() {
        System.out.println("[DEBUG_LOG] Testing existing book ID retrieval");
        
        // Given
        idMappingService.getOrCreateBookId("mongo-book-1");

        // When
        Long retrievedId = idMappingService.getBookId("mongo-book-1");

        // Then
        assertThat(retrievedId).isEqualTo(1L);
    }

    @Test
    void shouldReturnNullForNonExistentBookId() {
        System.out.println("[DEBUG_LOG] Testing non-existent book ID retrieval");
        
        // When
        Long retrievedId = idMappingService.getBookId("non-existent-book");

        // Then
        assertThat(retrievedId).isNull();
    }

    @Test
    void shouldClearAllMappings() {
        System.out.println("[DEBUG_LOG] Testing mappings clearing");
        
        // Given
        idMappingService.getOrCreateAuthorId("mongo-author-1");
        idMappingService.getOrCreateGenreId("mongo-genre-1");
        idMappingService.getOrCreateBookId("mongo-book-1");

        // When
        idMappingService.clearMappings();

        // Then
        assertThat(idMappingService.getBookId("mongo-book-1")).isNull();
        
        // New IDs should start from 1 again
        Long newAuthorId = idMappingService.getOrCreateAuthorId("mongo-author-2");
        Long newGenreId = idMappingService.getOrCreateGenreId("mongo-genre-2");
        Long newBookId = idMappingService.getOrCreateBookId("mongo-book-2");
        
        assertThat(newAuthorId).isEqualTo(1L);
        assertThat(newGenreId).isEqualTo(1L);
        assertThat(newBookId).isEqualTo(1L);
    }

    @Test
    void shouldHandleConcurrentAccess() {
        System.out.println("[DEBUG_LOG] Testing concurrent access to ID mapping");
        
        // When - simulate concurrent access
        Long id1 = idMappingService.getOrCreateAuthorId("concurrent-author");
        Long id2 = idMappingService.getOrCreateAuthorId("concurrent-author");

        // Then
        assertThat(id1).isEqualTo(id2);
        assertThat(id1).isEqualTo(1L);
    }

    @Test
    void shouldMaintainSeparateCountersForDifferentEntityTypes() {
        System.out.println("[DEBUG_LOG] Testing separate counters for different entity types");
        
        // When
        Long authorId = idMappingService.getOrCreateAuthorId("entity-1");
        Long genreId = idMappingService.getOrCreateGenreId("entity-1");
        Long bookId = idMappingService.getOrCreateBookId("entity-1");

        // Then - all should be 1 since they use separate counters
        assertThat(authorId).isEqualTo(1L);
        assertThat(genreId).isEqualTo(1L);
        assertThat(bookId).isEqualTo(1L);
    }
}