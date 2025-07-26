package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MongoConfig {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    @PostConstruct
    public void initializeDatabase() {
        log.info("[DEBUG_LOG] Starting database initialization...");
        
        try {
            // Clear existing data and create new data - using block() to ensure completion
            List<Book> books = bookRepository.deleteAll()
                    .doOnSuccess(v -> log.info("[DEBUG_LOG] Deleted all books"))
                    .then(authorRepository.deleteAll())
                    .doOnSuccess(v -> log.info("[DEBUG_LOG] Deleted all authors"))
                    .then(genreRepository.deleteAll())
                    .doOnSuccess(v -> log.info("[DEBUG_LOG] Deleted all genres"))
                    .then(createAuthors().collectList())
                    .doOnSuccess(authors -> log.info("[DEBUG_LOG] Created {} authors: {}", 
                            authors.size(), authors.stream().map(Author::getFullName).toList()))
                    .flatMap(authors -> createGenres().collectList()
                            .doOnSuccess(genres -> log.info("[DEBUG_LOG] Created {} genres: {}", 
                                    genres.size(), genres.stream().map(Genre::getName).toList()))
                            .flatMap(genres -> createBooks(authors, genres).collectList()
                                    .doOnSuccess(createdBooks -> log.info("[DEBUG_LOG] Created {} books: {}", 
                                            createdBooks.size(), createdBooks.stream().map(Book::getTitle).toList()))))
                    .doOnSuccess(finalBooks -> log.info("[DEBUG_LOG] Database initialized successfully with {} books", finalBooks.size()))
                    .doOnError(error -> log.error("[DEBUG_LOG] Failed to initialize database", error))
                    .block(); // Block to ensure completion during startup
            
            log.info("[DEBUG_LOG] Database initialization completed successfully. Total books: {}", 
                    books != null ? books.size() : 0);
            
            // Verify data was actually saved
            verifyDataAfterInitialization();
            
        } catch (Exception e) {
            log.error("[DEBUG_LOG] Exception during database initialization", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    private void verifyDataAfterInitialization() {
        try {
            Long authorCount = authorRepository.count().block();
            Long genreCount = genreRepository.count().block();
            Long bookCount = bookRepository.count().block();
            
            log.info("[DEBUG_LOG] Verification - Authors in DB: {}, Genres in DB: {}, Books in DB: {}", 
                    authorCount, genreCount, bookCount);
            
            if (authorCount == 0 || genreCount == 0 || bookCount == 0) {
                log.error("[DEBUG_LOG] WARNING: Some collections are empty after initialization!");
            }
        } catch (Exception e) {
            log.error("[DEBUG_LOG] Failed to verify data after initialization", e);
        }
    }

    private Flux<Author> createAuthors() {
        return Flux.just(
                new Author(null, "Author 1"),
                new Author(null, "Author 2"),
                new Author(null, "Author 3")
        ).flatMap(authorRepository::save);
    }

    private Flux<Genre> createGenres() {
        return Flux.just(
                new Genre(null, "Genre 1"),
                new Genre(null, "Genre 2"),
                new Genre(null, "Genre 3"),
                new Genre(null, "Genre 4"),
                new Genre(null, "Genre 5"),
                new Genre(null, "Genre 6")
        ).flatMap(genreRepository::save);
    }

    private Flux<Book> createBooks(List<Author> authors, List<Genre> genres) {
        return Flux.just(
                new Book(null, "Book 1", authors.get(0), List.of(genres.get(0), genres.get(1))),
                new Book(null, "Book 2", authors.get(1), List.of(genres.get(2), genres.get(3))),
                new Book(null, "Book 3", authors.get(2), List.of(genres.get(4), genres.get(5)))
        ).flatMap(bookRepository::save);
    }
}