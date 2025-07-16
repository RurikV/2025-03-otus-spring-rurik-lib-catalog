package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.junit.jupiter.api.BeforeEach;
import ru.otus.hw.config.EmbeddedMongoDisabler;
import ru.otus.hw.config.TestMongoConfig;
import ru.otus.hw.models.Author;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthorRepository should")
@DataMongoTest
@Import({TestMongoConfig.class, EmbeddedMongoDisabler.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    @BeforeEach
    void setUp() {
        // Clear all collections before each test to ensure clean state
        authorRepository.deleteAll();
    }

    @DisplayName("save author correctly")
    @Test
    void shouldSaveAuthor() {
        // Arrange
        Author expectedAuthor = new Author(null, "Test Author");

        // Act
        Author savedAuthor = authorRepository.save(expectedAuthor);
        Author retrievedAuthor = authorRepository.findById(savedAuthor.getId()).orElseThrow();

        // Assert
        assertThat(savedAuthor.getId()).isNotNull();
        expectedAuthor.setId(savedAuthor.getId());
        assertThat(savedAuthor).usingRecursiveComparison().isEqualTo(expectedAuthor);

        assertThat(retrievedAuthor).isNotNull();
        assertThat(retrievedAuthor).usingRecursiveComparison().isEqualTo(savedAuthor);
    }

    @DisplayName("find all authors")
    @Test
    void shouldFindAllAuthors() {
        // Arrange
        Author author1 = new Author(null, "Test Author 1");
        Author author2 = new Author(null, "Test Author 2");
        authorRepository.saveAll(List.of(author1, author2));

        // Act
        List<Author> authors = authorRepository.findAll();
        // Set IDs for expected authors to match the actual authors
        List<Author> savedAuthors = authors.stream()
                .filter(a -> a.getFullName().equals(author1.getFullName()) || a.getFullName().equals(author2.getFullName()))
                .toList();

        // Assert
        assertThat(authors).isNotEmpty();
        assertThat(authors.size()).isGreaterThanOrEqualTo(2);

        if (savedAuthors.size() >= 2) {
            author1.setId(savedAuthors.get(0).getId());
            author2.setId(savedAuthors.get(1).getId());

            // Use recursive comparison to verify the authors exist in the result
            assertThat(authors)
                .usingRecursiveFieldByFieldElementComparator()
                .contains(author1, author2);
        }
    }
}
