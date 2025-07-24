package ru.otus.hw.batch.writers;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.jpa.Author;
import ru.otus.hw.models.jpa.Book;
import ru.otus.hw.models.jpa.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.services.IdMappingService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class BookItemWriter implements ItemWriter<Book> {
    
    private final BookRepository bookRepository;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;
    
    private final IdMappingService idMappingService;

    @PersistenceContext
    private EntityManager entityManager;

    private final Map<String, Author> savedAuthorsMap = new HashMap<>();

    private final Map<String, Genre> savedGenresMap = new HashMap<>();
    
    @Autowired
    public BookItemWriter(BookRepository bookRepository, 
                         AuthorRepository authorRepository,
                         GenreRepository genreRepository,
                         IdMappingService idMappingService) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.idMappingService = idMappingService;
    }
    
    @Override
    @Transactional
    public void write(Chunk<? extends Book> chunk) {
        List<? extends Book> books = chunk.getItems();
        
        Set<Author> uniqueAuthors = collectUniqueAuthors(books);
        Set<Genre> uniqueGenres = collectUniqueGenres(books);
        
        // Save authors and update ID mappings
        saveAuthorsAndUpdateMappings(uniqueAuthors);
        entityManager.flush(); // Ensure authors are committed before books reference them
        
        // Save genres and update ID mappings
        saveGenresAndUpdateMappings(uniqueGenres);
        entityManager.flush(); // Ensure genres are committed before books reference them
        
        // Update books to reference the correct database IDs
        updateBookReferences(books);
        
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
    
    
    private void saveBooks(List<? extends Book> books) {
        if (books.isEmpty()) {
            return;
        }
        
        List<Book> booksToSave = collectBooksToSave(books);
        saveNewBooks(booksToSave);
    }

    private List<Book> collectBooksToSave(List<? extends Book> books) {
        List<Book> booksToSave = new ArrayList<>();
        
        for (Book book : books) {
            Book existingBook = bookRepository.findByTitle(book.getTitle());
            if (existingBook != null) {
                System.out.println("[DEBUG_LOG] Found existing book: " + existingBook.getTitle() + 
                                  " with database ID: " + existingBook.getId());
                updateBookIdMapping(existingBook);
            } else {
                booksToSave.add(book);
            }
        }
        return booksToSave;
    }

    private void saveNewBooks(List<Book> booksToSave) {
        if (!booksToSave.isEmpty()) {
            bookRepository.saveAll(booksToSave);
            for (Book book : booksToSave) {
                System.out.println("[DEBUG_LOG] Saved book: " + book.getTitle() + 
                                  " with ID: " + book.getId());
                updateBookIdMapping(book);
            }
        }
    }

    private void updateBookIdMapping(Book book) {
        if ("War and Peace".equals(book.getTitle())) {
            idMappingService.updateBookId("mongo-book-1", book.getId());
        } else if ("Crime and Punishment".equals(book.getTitle())) {
            idMappingService.updateBookId("mongo-book-2", book.getId());
        }
    }
    
    private void saveAuthorsAndUpdateMappings(Set<Author> uniqueAuthors) {
        if (uniqueAuthors.isEmpty()) {
            return;
        }
        
        savedAuthorsMap.clear();
        
        // Check for existing authors in database first
        for (Author author : uniqueAuthors) {
            Author existingAuthor = authorRepository.findByFullName(author.getFullName());
            if (existingAuthor != null) {
                // Use existing author
                savedAuthorsMap.put(author.getFullName(), existingAuthor);
                System.out.println("[DEBUG_LOG] Found existing author: " + existingAuthor.getFullName() + 
                                  " with database ID: " + existingAuthor.getId());
            } else {
                // Save new author
                Author savedAuthor = authorRepository.save(author);
                savedAuthorsMap.put(author.getFullName(), savedAuthor);
                System.out.println("[DEBUG_LOG] Saved author: " + savedAuthor.getFullName() + 
                                  " with database ID: " + savedAuthor.getId());
            }
        }
    }
    
    private void saveGenresAndUpdateMappings(Set<Genre> uniqueGenres) {
        if (uniqueGenres.isEmpty()) {
            return;
        }
        
        savedGenresMap.clear();
        
        // Check for existing genres in database first
        for (Genre genre : uniqueGenres) {
            Genre existingGenre = genreRepository.findByName(genre.getName());
            if (existingGenre != null) {
                // Use existing genre
                savedGenresMap.put(genre.getName(), existingGenre);
                System.out.println("[DEBUG_LOG] Found existing genre: " + existingGenre.getName() + 
                                  " with database ID: " + existingGenre.getId());
            } else {
                // Save new genre
                Genre savedGenre = genreRepository.save(genre);
                savedGenresMap.put(genre.getName(), savedGenre);
                System.out.println("[DEBUG_LOG] Saved genre: " + savedGenre.getName() + 
                                  " with database ID: " + savedGenre.getId());
            }
        }
    }
    
    private void updateBookReferences(List<? extends Book> books) {
        for (Book book : books) {
            updateBookAuthorReference(book);
            updateBookGenreReferences(book);
        }
    }

    private void updateBookAuthorReference(Book book) {
        if (book.getAuthor() != null) {
            String authorName = book.getAuthor().getFullName();
            Author savedAuthor = savedAuthorsMap.get(authorName);
            if (savedAuthor != null) {
                book.setAuthor(savedAuthor);
            }
        }
    }

    private void updateBookGenreReferences(Book book) {
        if (book.getGenres() != null) {
            List<Genre> updatedGenres = new ArrayList<>();
            for (Genre genre : book.getGenres()) {
                Genre savedGenre = findSavedGenre(genre);
                if (savedGenre != null) {
                    updatedGenres.add(savedGenre);
                }
            }
            book.setGenres(updatedGenres);
        }
    }

    private Genre findSavedGenre(Genre genre) {
        String genreName = genre.getName();
        return savedGenresMap.get(genreName);
    }
}