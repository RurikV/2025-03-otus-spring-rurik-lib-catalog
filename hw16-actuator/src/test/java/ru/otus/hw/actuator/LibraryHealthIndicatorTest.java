package ru.otus.hw.actuator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import ru.otus.hw.repositories.BookRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("LibraryHealthIndicator")
class LibraryHealthIndicatorTest {

    @Test
    @DisplayName("returns UP with books.count when repository works")
    void shouldReturnUpWhenRepoAccessible() {
        BookRepository repo = mock(BookRepository.class);
        when(repo.count()).thenReturn(7L);
        LibraryHealthIndicator indicator = new LibraryHealthIndicator(repo);

        Health health = indicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("UP");
        assertThat(health.getDetails()).containsEntry("component", "library");
        assertThat(health.getDetails()).containsEntry("books.count", 7L);
        assertThat(health.getDetails()).containsKey("timestamp");
    }

    @Test
    @DisplayName("returns DOWN with error when repository throws")
    void shouldReturnDownWhenRepoFails() {
        BookRepository repo = mock(BookRepository.class);
        RuntimeException ex = new RuntimeException("boom");
        when(repo.count()).thenThrow(ex);
        LibraryHealthIndicator indicator = new LibraryHealthIndicator(repo);

        Health health = indicator.health();

        assertThat(health.getStatus().getCode()).isEqualTo("DOWN");
        assertThat(health.getDetails()).containsEntry("component", "library");
        assertThat(health.getDetails()).containsEntry("error", "boom");
    }
}
