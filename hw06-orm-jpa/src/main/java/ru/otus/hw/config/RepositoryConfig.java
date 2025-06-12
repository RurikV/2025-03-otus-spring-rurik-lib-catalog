package ru.otus.hw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.repositories.JpaAuthorRepository;
import ru.otus.hw.repositories.JpaBookRepository;
import ru.otus.hw.repositories.JpaGenreRepository;

@Configuration
public class RepositoryConfig {

    @Bean
    @Primary
    public AuthorRepository authorRepository(JpaAuthorRepository jpaAuthorRepository) {
        return jpaAuthorRepository;
    }

    @Bean
    @Primary
    public BookRepository bookRepository(JpaBookRepository jpaBookRepository) {
        return jpaBookRepository;
    }

    @Bean
    @Primary
    public GenreRepository genreRepository(JpaGenreRepository jpaGenreRepository) {
        return jpaGenreRepository;
    }
}