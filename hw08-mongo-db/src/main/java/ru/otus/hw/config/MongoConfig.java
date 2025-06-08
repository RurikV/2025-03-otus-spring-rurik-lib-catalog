package ru.otus.hw.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class MongoConfig {

    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final BookRepository bookRepository;

    @Bean
    public List<Book> initializeDatabase() {
        // Clear existing data
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();

        // Create authors
        Author author1 = authorRepository.save(new Author(null, "Author 1"));
        Author author2 = authorRepository.save(new Author(null, "Author 2"));
        Author author3 = authorRepository.save(new Author(null, "Author 3"));

        // Create genres
        Genre genre1 = genreRepository.save(new Genre(null, "Genre 1"));
        Genre genre2 = genreRepository.save(new Genre(null, "Genre 2"));
        Genre genre3 = genreRepository.save(new Genre(null, "Genre 3"));
        Genre genre4 = genreRepository.save(new Genre(null, "Genre 4"));
        Genre genre5 = genreRepository.save(new Genre(null, "Genre 5"));
        Genre genre6 = genreRepository.save(new Genre(null, "Genre 6"));

        // Create books
        Book book1 = new Book(null, "Book 1", author1, List.of(genre1, genre2));
        Book book2 = new Book(null, "Book 2", author2, List.of(genre3, genre4));
        Book book3 = new Book(null, "Book 3", author3, List.of(genre5, genre6));

        return bookRepository.saveAll(List.of(book1, book2, book3));
    }
}
