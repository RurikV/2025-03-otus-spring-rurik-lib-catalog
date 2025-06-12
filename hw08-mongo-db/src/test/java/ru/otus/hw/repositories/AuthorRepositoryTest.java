package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.config.EmbeddedMongoDisabler;
import ru.otus.hw.config.TestMongoConfig;
import ru.otus.hw.models.Author;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthorRepository should")
@DataMongoTest
@Import({TestMongoConfig.class, EmbeddedMongoDisabler.class})
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

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
        assertThat(savedAuthor.getFullName()).isEqualTo(expectedAuthor.getFullName());

        assertThat(retrievedAuthor).isNotNull();
        assertThat(retrievedAuthor.getId()).isEqualTo(savedAuthor.getId());
        assertThat(retrievedAuthor.getFullName()).isEqualTo(expectedAuthor.getFullName());
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

        // Assert
        assertThat(authors).isNotEmpty();
        assertThat(authors.size()).isGreaterThanOrEqualTo(2);
    }
}
