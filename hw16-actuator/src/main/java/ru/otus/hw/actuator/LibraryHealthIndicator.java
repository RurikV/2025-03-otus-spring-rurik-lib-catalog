package ru.otus.hw.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import ru.otus.hw.repositories.BookRepository;

import java.time.OffsetDateTime;

@Component
public class LibraryHealthIndicator implements HealthIndicator {

    private final BookRepository bookRepository;

    public LibraryHealthIndicator(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Health health() {
        try {
            long booksCount = bookRepository.count();
            return Health.up()
                    .withDetail("component", "library")
                    .withDetail("books.count", booksCount)
                    .withDetail("timestamp", OffsetDateTime.now().toString())
                    .build();
        } catch (Exception e) {
            return Health.down(e)
                    .withDetail("component", "library")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
