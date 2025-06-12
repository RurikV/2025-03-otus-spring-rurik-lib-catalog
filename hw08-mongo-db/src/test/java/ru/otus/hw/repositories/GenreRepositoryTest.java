package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.config.EmbeddedMongoDisabler;
import ru.otus.hw.config.TestMongoConfig;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GenreRepository should")
@DataMongoTest
@Import({TestMongoConfig.class, EmbeddedMongoDisabler.class})
class GenreRepositoryTest {

    @Autowired
    private GenreRepository genreRepository;

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
        assertThat(savedGenre.getName()).isEqualTo(expectedGenre.getName());

        assertThat(retrievedGenre).isNotNull();
        assertThat(retrievedGenre.getId()).isEqualTo(savedGenre.getId());
        assertThat(retrievedGenre.getName()).isEqualTo(expectedGenre.getName());
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

        // Assert
        assertThat(genres).isNotEmpty();
        assertThat(genres.size()).isGreaterThanOrEqualTo(2);
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
        Set<String> foundGenreIds = foundGenres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        assertThat(foundGenreIds).containsExactlyInAnyOrderElementsOf(genreIds);
    }
}
