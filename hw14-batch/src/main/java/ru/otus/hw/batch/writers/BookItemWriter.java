package ru.otus.hw.batch.writers;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.jpa.Author;
import ru.otus.hw.models.jpa.Book;
import ru.otus.hw.models.jpa.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class BookItemWriter implements ItemWriter<Book> {
    
    private final BookRepository bookRepository;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;
    
    @Autowired
    public BookItemWriter(BookRepository bookRepository, 
                         AuthorRepository authorRepository,
                         GenreRepository genreRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
    }
    
    @Override
    public void write(Chunk<? extends Book> chunk) throws Exception {
        List<? extends Book> books = chunk.getItems();
        
        Set<Author> uniqueAuthors = collectUniqueAuthors(books);
        Set<Genre> uniqueGenres = collectUniqueGenres(books);
        
        saveAuthors(uniqueAuthors);
        saveGenres(uniqueGenres);
        saveBooks(books);
    }
    
    private Set<Author> collectUniqueAuthors(List<? extends Book> books) {
        Set<Author> uniqueAuthors = new HashSet<>();
        for (Book book : books) {
            if (book.getAuthor() != null) {
                uniqueAuthors.add(book.getAuthor());
            }
        }
        return uniqueAuthors;
    }
    
    private Set<Genre> collectUniqueGenres(List<? extends Book> books) {
        Set<Genre> uniqueGenres = new HashSet<>();
        for (Book book : books) {
            if (book.getGenres() != null) {
                uniqueGenres.addAll(book.getGenres());
            }
        }
        return uniqueGenres;
    }
    
    private void saveAuthors(Set<Author> uniqueAuthors) {
        for (Author author : uniqueAuthors) {
            if (!authorRepository.existsById(author.getId())) {
                authorRepository.save(author);
                System.out.println("[DEBUG_LOG] Saved author: " + author.getFullName());
            }
        }
    }
    
    private void saveGenres(Set<Genre> uniqueGenres) {
        for (Genre genre : uniqueGenres) {
            if (!genreRepository.existsById(genre.getId())) {
                genreRepository.save(genre);
                System.out.println("[DEBUG_LOG] Saved genre: " + genre.getName());
            }
        }
    }
    
    private void saveBooks(List<? extends Book> books) {
        for (Book book : books) {
            bookRepository.save(book);
            System.out.println("[DEBUG_LOG] Saved book: " + book.getTitle() + 
                              " with ID: " + book.getId());
        }
    }
}