package ru.otus.hw.batch.writers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class BookItemWriter implements ItemWriter<Book> {
    
    private static final Logger logger = LoggerFactory.getLogger(BookItemWriter.class);
    
    private final BookRepository bookRepository;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;
    
    private final IdMappingService idMappingService;

    @PersistenceContext
    private EntityManager entityManager;
    
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
        
        // Pre-load all existing entities to avoid database queries during transformation
        Set<String> bookTitles = collectBookTitles(books);
        Set<String> authorNames = collectAuthorNames(books);
        Set<String> genreNames = collectGenreNames(books);
        
        Map<String, Book> existingBooksMap = preloadExistingBooks(bookTitles);
        Map<String, Author> existingAuthorsMap = preloadExistingAuthors(authorNames);
        Map<String, Genre> existingGenresMap = preloadExistingGenres(genreNames);
        
        Set<Author> uniqueAuthors = collectUniqueAuthors(books);
        Set<Genre> uniqueGenres = collectUniqueGenres(books);
        
        // Create local maps for this chunk processing
        Map<String, Author> savedAuthorsMap = new HashMap<>();
        Map<String, Genre> savedGenresMap = new HashMap<>();
        
        // Process authors and genres in parallel
        CompletableFuture<Void> authorsFuture = CompletableFuture.runAsync(() -> {
            saveAuthorsAndUpdateMappings(uniqueAuthors, savedAuthorsMap, existingAuthorsMap);
        });
        
        CompletableFuture<Void> genresFuture = CompletableFuture.runAsync(() -> {
            saveGenresAndUpdateMappings(uniqueGenres, savedGenresMap, existingGenresMap);
        });
        
        try {
            // Wait for both authors and genres processing to complete
            CompletableFuture.allOf(authorsFuture, genresFuture).get();
            logger.debug("Parallel processing of authors and genres completed");
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error during parallel processing of authors and genres", e);
            throw new RuntimeException("Failed to process authors and genres in parallel", e);
        }
        
        // Ensure all entities are committed before books reference them
        entityManager.flush();
        
        // Update books to reference the correct database IDs
        updateBookReferences(books, savedAuthorsMap, savedGenresMap);
        
        saveBooks(books, existingBooksMap);
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
    
    private Set<String> collectBookTitles(List<? extends Book> books) {
        Set<String> titles = new HashSet<>();
        for (Book book : books) {
            if (book.getTitle() != null) {
                titles.add(book.getTitle());
            }
        }
        return titles;
    }
    
    private Set<String> collectAuthorNames(List<? extends Book> books) {
        Set<String> authorNames = new HashSet<>();
        for (Book book : books) {
            if (book.getAuthor() != null && book.getAuthor().getFullName() != null) {
                authorNames.add(book.getAuthor().getFullName());
            }
        }
        return authorNames;
    }
    
    private Set<String> collectGenreNames(List<? extends Book> books) {
        Set<String> genreNames = new HashSet<>();
        for (Book book : books) {
            if (book.getGenres() != null) {
                for (Genre genre : book.getGenres()) {
                    if (genre.getName() != null) {
                        genreNames.add(genre.getName());
                    }
                }
            }
        }
        return genreNames;
    }
    
    private Map<String, Book> preloadExistingBooks(Set<String> titles) {
        Map<String, Book> existingBooks = new HashMap<>();
        if (!titles.isEmpty()) {
            List<Book> books = bookRepository.findByTitleIn(new ArrayList<>(titles));
            for (Book book : books) {
                existingBooks.put(book.getTitle(), book);
            }
        }
        return existingBooks;
    }
    
    private Map<String, Author> preloadExistingAuthors(Set<String> authorNames) {
        Map<String, Author> existingAuthors = new HashMap<>();
        if (!authorNames.isEmpty()) {
            List<Author> authors = authorRepository.findByFullNameIn(new ArrayList<>(authorNames));
            for (Author author : authors) {
                existingAuthors.put(author.getFullName(), author);
            }
        }
        return existingAuthors;
    }
    
    private Map<String, Genre> preloadExistingGenres(Set<String> genreNames) {
        Map<String, Genre> existingGenres = new HashMap<>();
        if (!genreNames.isEmpty()) {
            List<Genre> genres = genreRepository.findByNameIn(new ArrayList<>(genreNames));
            for (Genre genre : genres) {
                existingGenres.put(genre.getName(), genre);
            }
        }
        return existingGenres;
    }
    
    
    private void saveBooks(List<? extends Book> books, Map<String, Book> existingBooksMap) {
        if (books.isEmpty()) {
            return;
        }
        
        List<Book> booksToSave = collectBooksToSave(books, existingBooksMap);
        saveNewBooks(booksToSave);
    }

    private List<Book> collectBooksToSave(List<? extends Book> books, Map<String, Book> existingBooksMap) {
        List<Book> booksToSave = new ArrayList<>();
        
        for (Book book : books) {
            Book existingBook = existingBooksMap.get(book.getTitle());
            if (existingBook != null) {
                logger.debug("Found existing book: {} with database ID: {}", existingBook.getTitle(), existingBook.getId());
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
                logger.debug("Saved book: {} with ID: {}", book.getTitle(), book.getId());
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
    
    private void saveAuthorsAndUpdateMappings(Set<Author> uniqueAuthors, Map<String, Author> savedAuthorsMap, Map<String, Author> existingAuthorsMap) {
        if (uniqueAuthors.isEmpty()) {
            return;
        }
        
        // Check for existing authors using pre-loaded data
        for (Author author : uniqueAuthors) {
            Author existingAuthor = existingAuthorsMap.get(author.getFullName());
            if (existingAuthor != null) {
                // Use existing author
                savedAuthorsMap.put(author.getFullName(), existingAuthor);
                logger.debug("Found existing author: {} with database ID: {}", existingAuthor.getFullName(), existingAuthor.getId());
            } else {
                // Save new author
                Author savedAuthor = authorRepository.save(author);
                savedAuthorsMap.put(author.getFullName(), savedAuthor);
                logger.debug("Saved author: {} with database ID: {}", savedAuthor.getFullName(), savedAuthor.getId());
            }
        }
    }
    
    private void saveGenresAndUpdateMappings(Set<Genre> uniqueGenres, Map<String, Genre> savedGenresMap, Map<String, Genre> existingGenresMap) {
        if (uniqueGenres.isEmpty()) {
            return;
        }
        
        // Check for existing genres using pre-loaded data
        for (Genre genre : uniqueGenres) {
            Genre existingGenre = existingGenresMap.get(genre.getName());
            if (existingGenre != null) {
                // Use existing genre
                savedGenresMap.put(genre.getName(), existingGenre);
                logger.debug("Found existing genre: {} with database ID: {}", existingGenre.getName(), existingGenre.getId());
            } else {
                // Save new genre
                Genre savedGenre = genreRepository.save(genre);
                savedGenresMap.put(genre.getName(), savedGenre);
                logger.debug("Saved genre: {} with database ID: {}", savedGenre.getName(), savedGenre.getId());
            }
        }
    }
    
    private void updateBookReferences(List<? extends Book> books, Map<String, Author> savedAuthorsMap, Map<String, Genre> savedGenresMap) {
        for (Book book : books) {
            updateBookAuthorReference(book, savedAuthorsMap);
            updateBookGenreReferences(book, savedGenresMap);
        }
    }

    private void updateBookAuthorReference(Book book, Map<String, Author> savedAuthorsMap) {
        if (book.getAuthor() != null) {
            String authorName = book.getAuthor().getFullName();
            Author savedAuthor = savedAuthorsMap.get(authorName);
            if (savedAuthor != null) {
                book.setAuthor(savedAuthor);
            }
        }
    }

    private void updateBookGenreReferences(Book book, Map<String, Genre> savedGenresMap) {
        if (book.getGenres() != null) {
            List<Genre> updatedGenres = new ArrayList<>();
            for (Genre genre : book.getGenres()) {
                Genre savedGenre = findSavedGenre(genre, savedGenresMap);
                if (savedGenre != null) {
                    updatedGenres.add(savedGenre);
                }
            }
            book.setGenres(updatedGenres);
        }
    }

    private Genre findSavedGenre(Genre genre, Map<String, Genre> savedGenresMap) {
        String genreName = genre.getName();
        return savedGenresMap.get(genreName);
    }
}