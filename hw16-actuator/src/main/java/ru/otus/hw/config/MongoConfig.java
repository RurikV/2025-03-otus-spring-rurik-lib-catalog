package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
    @Profile("!test")
    public List<Book> initializeDatabase() {
        // Clear existing data
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();

        // Create data
        List<Author> authors = createAuthors();
        List<Genre> genres = createGenres();
        List<Book> books = createBooks(authors, genres);

        return bookRepository.saveAll(books);
    }

    private List<Author> createAuthors() {
        Author author1 = authorRepository.save(new Author(null, "Author 1"));
        Author author2 = authorRepository.save(new Author(null, "Author 2"));
        Author author3 = authorRepository.save(new Author(null, "Author 3"));
        return List.of(author1, author2, author3);
    }

    private List<Genre> createGenres() {
        Genre genre1 = genreRepository.save(new Genre(null, "Genre 1"));
        Genre genre2 = genreRepository.save(new Genre(null, "Genre 2"));
        Genre genre3 = genreRepository.save(new Genre(null, "Genre 3"));
        Genre genre4 = genreRepository.save(new Genre(null, "Genre 4"));
        Genre genre5 = genreRepository.save(new Genre(null, "Genre 5"));
        Genre genre6 = genreRepository.save(new Genre(null, "Genre 6"));
        return List.of(genre1, genre2, genre3, genre4, genre5, genre6);
    }

    private List<Book> createBooks(List<Author> authors, List<Genre> genres) {
        Book book1 = new Book(null, "Book 1", authors.get(0), List.of(genres.get(0), genres.get(1)));
        Book book2 = new Book(null, "Book 2", authors.get(1), List.of(genres.get(2), genres.get(3)));
        Book book3 = new Book(null, "Book 3", authors.get(2), List.of(genres.get(4), genres.get(5)));
        return List.of(book1, book2, book3);
    }
}
