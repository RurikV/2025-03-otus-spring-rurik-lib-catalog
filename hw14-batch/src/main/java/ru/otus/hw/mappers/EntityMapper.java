package ru.otus.hw.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.jpa.Author;
import ru.otus.hw.models.jpa.Book;
import ru.otus.hw.models.jpa.Comment;
import ru.otus.hw.models.jpa.Genre;
import ru.otus.hw.models.mongo.MongoAuthor;
import ru.otus.hw.models.mongo.MongoBook;
import ru.otus.hw.models.mongo.MongoComment;
import ru.otus.hw.models.mongo.MongoGenre;
import ru.otus.hw.services.IdMappingService;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityMapper {
    
    private final IdMappingService idMappingService;
    
    @Autowired
    public EntityMapper(IdMappingService idMappingService) {
        this.idMappingService = idMappingService;
    }
    
    public Author mapToAuthor(MongoAuthor mongoAuthor) {
        if (mongoAuthor == null) {
            return null;
        }
        
        Author author = new Author();
        // Don't set ID - let database generate it with IDENTITY strategy
        author.setFullName(mongoAuthor.getFullName());
        return author;
    }
    
    public Genre mapToGenre(MongoGenre mongoGenre) {
        if (mongoGenre == null) {
            return null;
        }
        
        Genre genre = new Genre();
        // Don't set ID - let database generate it with IDENTITY strategy
        genre.setName(mongoGenre.getName());
        return genre;
    }
    
    public Book mapToBook(MongoBook mongoBook) {
        if (mongoBook == null) {
            return null;
        }
        
        Book book = new Book();
        // Don't set ID - let database generate it with IDENTITY strategy
        book.setTitle(mongoBook.getTitle());
        
        // Map embedded author
        if (mongoBook.getAuthor() != null) {
            book.setAuthor(mapToAuthor(mongoBook.getAuthor()));
        }
        
        // Map embedded genres
        if (mongoBook.getGenres() != null) {
            List<Genre> genres = mongoBook.getGenres().stream()
                    .map(this::mapToGenre)
                    .collect(Collectors.toList());
            book.setGenres(genres);
        }
        
        return book;
    }
    
    public Comment mapToComment(MongoComment mongoComment) {
        if (mongoComment == null) {
            return null;
        }
        
        Comment comment = new Comment();
        comment.setText(mongoComment.getText());
        
        // Map book reference - we need to create a book with just the ID
        if (mongoComment.getBook() != null) {
            String mongoBookId = mongoComment.getBook().getId();
            Long bookId = idMappingService.getBookId(mongoBookId);
            System.out.println("[DEBUG_LOG] Mapping comment for MongoDB book ID: " + mongoBookId + 
                              " -> JPA book ID: " + bookId);
            
            Book book = new Book();
            book.setId(bookId);
            comment.setBook(book);
        }
        
        return comment;
    }
}