package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.junit.jupiter.api.BeforeEach;
import ru.otus.hw.config.EmbeddedMongoDisabler;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GenreRepository should")
@DataMongoTest
@Import({EmbeddedMongoDisabler.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GenreRepositoryTest {

    @Autowired
    private GenreRepository genreRepository;

    @BeforeEach
    void setUp() {
        // Clear all collections before each test to ensure clean state
        genreRepository.deleteAll();
    }

    @DisplayName("save genre correctly")
    @Test
    void shouldSaveGenre() {
        // Arrange
        Genre expectedGenre = new Genre(null, "Test Genre");

        // Act
        Genre savedGenre = genreRepository.save(expectedGenre);
        Genre retrievedGenre = genreRepository.findById(savedGenre.getId()).orElseThrow();

        // Assert
        assertThat(savedGenre.getId()).isNotNull();
        expectedGenre.setId(savedGenre.getId());
        assertThat(savedGenre).usingRecursiveComparison().isEqualTo(expectedGenre);

        assertThat(retrievedGenre).isNotNull();
        assertThat(retrievedGenre).usingRecursiveComparison().isEqualTo(savedGenre);
    }

    @DisplayName("find all genres")
    @Test
    void shouldFindAllGenres() {
        // Arrange
        Genre genre1 = new Genre(null, "Test Genre 1");
        Genre genre2 = new Genre(null, "Test Genre 2");
        genreRepository.saveAll(List.of(genre1, genre2));

        // Act
        List<Genre> genres = genreRepository.findAll();
        // Set IDs for expected genres to match the actual genres
        List<Genre> savedGenres = genres.stream()
                .filter(g -> g.getName().equals(genre1.getName()) || g.getName().equals(genre2.getName()))
                .toList();
        if (savedGenres.size() >= 2) {
            genre1.setId(savedGenres.get(0).getId());
            genre2.setId(savedGenres.get(1).getId());
        }

        // Assert
        assertThat(genres).isNotEmpty();
        assertThat(genres.size()).isGreaterThanOrEqualTo(2);


        if (savedGenres.size() >= 2) {
            // Use recursive comparison to verify the genres exist in the result
            assertThat(genres)
                .usingRecursiveFieldByFieldElementComparator()
                .contains(genre1, genre2);
        }
    }

    @DisplayName("find all genres by ids")
    @Test
    void shouldFindAllGenresByIds() {
        // Arrange
        Genre genre1 = genreRepository.save(new Genre(null, "Test Genre 1"));
        Genre genre2 = genreRepository.save(new Genre(null, "Test Genre 2"));
        Set<String> genreIds = Set.of(genre1.getId(), genre2.getId());

        // Act
        List<Genre> foundGenres = genreRepository.findAllByIds(genreIds);

        // Assert
        assertThat(foundGenres).hasSize(2);

        // Use recursive comparison to verify all fields match
        assertThat(foundGenres)
            .usingRecursiveFieldByFieldElementComparator()
            .containsExactlyInAnyOrderElementsOf(List.of(genre1, genre2));
    }
}
