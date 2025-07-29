package ru.otus.hw.batch.processors;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.otus.hw.mappers.EntityMapper;
import ru.otus.hw.models.jpa.Book;
import ru.otus.hw.models.mongo.MongoBook;

@Component
public class BookItemProcessor implements ItemProcessor<MongoBook, Book> {

    private final EntityMapper entityMapper;

    @Autowired
    public BookItemProcessor(EntityMapper entityMapper) {
        this.entityMapper = entityMapper;
    }

    @Override
    public Book process(MongoBook mongoBook) throws Exception {
        if (mongoBook == null) {
            return null;
        }
        
        // Transform MongoDB book to JPA book
        Book book = entityMapper.mapToBook(mongoBook);
        
        // Log the transformation for debugging
        System.out.println("[DEBUG_LOG] Processing book: " + mongoBook.getTitle() + 
                          " -> ID: " + book.getId());
        
        return book;
    }
}