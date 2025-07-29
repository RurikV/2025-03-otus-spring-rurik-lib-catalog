package ru.otus.hw.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.mongo.MongoAuthor;
import ru.otus.hw.models.mongo.MongoBook;
import ru.otus.hw.models.mongo.MongoComment;
import ru.otus.hw.models.mongo.MongoGenre;

import java.util.Arrays;

@Component
public class DataLoader implements CommandLineRunner {
    
    private final MongoTemplate mongoTemplate;
    
    @Autowired
    public DataLoader(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    @Override
    public void run(String... args) {
        clearExistingData();
        createTestAuthors();
        createTestGenres();
        createTestBooks();
        createTestComments();
        logDataLoadingResults();
    }
    
    private void clearExistingData() {
        mongoTemplate.dropCollection(MongoBook.class);
        mongoTemplate.dropCollection(MongoAuthor.class);
        mongoTemplate.dropCollection(MongoGenre.class);
        mongoTemplate.dropCollection(MongoComment.class);
    }
    
    private void createTestAuthors() {
        MongoAuthor author1 = new MongoAuthor("author1", "Leo Tolstoy");
        MongoAuthor author2 = new MongoAuthor("author2", "Fyodor Dostoevsky");
        MongoAuthor author3 = new MongoAuthor("author3", "Alexander Pushkin");
        
        mongoTemplate.save(author1);
        mongoTemplate.save(author2);
        mongoTemplate.save(author3);
    }
    
    private void createTestGenres() {
        MongoGenre genre1 = new MongoGenre("genre1", "Classic Literature");
        MongoGenre genre2 = new MongoGenre("genre2", "Philosophy");
        MongoGenre genre3 = new MongoGenre("genre3", "Romance");
        MongoGenre genre4 = new MongoGenre("genre4", "Drama");
        
        mongoTemplate.save(genre1);
        mongoTemplate.save(genre2);
        mongoTemplate.save(genre3);
        mongoTemplate.save(genre4);
    }
    
    private void createTestBooks() {
        MongoAuthor author1 = new MongoAuthor("author1", "Leo Tolstoy");
        MongoAuthor author2 = new MongoAuthor("author2", "Fyodor Dostoevsky");
        MongoAuthor author3 = new MongoAuthor("author3", "Alexander Pushkin");
        
        MongoGenre genre1 = new MongoGenre("genre1", "Classic Literature");
        MongoGenre genre2 = new MongoGenre("genre2", "Philosophy");
        MongoGenre genre3 = new MongoGenre("genre3", "Romance");
        MongoGenre genre4 = new MongoGenre("genre4", "Drama");
        
        MongoBook book1 = new MongoBook("book1", "War and Peace", author1, Arrays.asList(genre1, genre4));
        MongoBook book2 = new MongoBook("book2", "Crime and Punishment", author2, Arrays.asList(genre1, genre2));
        MongoBook book3 = new MongoBook("book3", "Eugene Onegin", author3, Arrays.asList(genre1, genre3));
        MongoBook book4 = new MongoBook("book4", "Anna Karenina", author1, Arrays.asList(genre1, genre3, genre4));
        
        mongoTemplate.save(book1);
        mongoTemplate.save(book2);
        mongoTemplate.save(book3);
        mongoTemplate.save(book4);
    }
    
    private void createTestComments() {
        MongoBook book1 = new MongoBook("book1", "War and Peace", null, null);
        MongoBook book2 = new MongoBook("book2", "Crime and Punishment", null, null);
        MongoBook book3 = new MongoBook("book3", "Eugene Onegin", null, null);
        MongoBook book4 = new MongoBook("book4", "Anna Karenina", null, null);
        
        MongoComment comment1 = new MongoComment("comment1", "Excellent masterpiece of Russian literature", book1);
        MongoComment comment2 = new MongoComment("comment2", "Deep psychological analysis", book2);
        MongoComment comment3 = new MongoComment("comment3", "Beautiful poetry and prose", book3);
        MongoComment comment4 = new MongoComment("comment4", "Tragic love story", book4);
        MongoComment comment5 = new MongoComment("comment5", "Another great work by Tolstoy", book4);
        
        mongoTemplate.save(comment1);
        mongoTemplate.save(comment2);
        mongoTemplate.save(comment3);
        mongoTemplate.save(comment4);
        mongoTemplate.save(comment5);
    }
    
    private void logDataLoadingResults() {
        System.out.println("[DEBUG_LOG] Test data loaded into MongoDB:");
        System.out.println("[DEBUG_LOG] - 3 authors");
        System.out.println("[DEBUG_LOG] - 4 genres");
        System.out.println("[DEBUG_LOG] - 4 books");
        System.out.println("[DEBUG_LOG] - 5 comments");
    }
}