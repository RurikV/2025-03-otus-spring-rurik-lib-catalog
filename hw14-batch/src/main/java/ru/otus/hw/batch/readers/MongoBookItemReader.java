package ru.otus.hw.batch.readers;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.mongo.MongoBook;

import java.util.Iterator;
import java.util.List;

@Component
public class MongoBookItemReader implements ItemReader<MongoBook> {
    
    private final MongoTemplate mongoTemplate;
    private Iterator<MongoBook> bookIterator;
    private boolean initialized = false;
    
    @Autowired
    public MongoBookItemReader(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    @Override
    public MongoBook read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!initialized) {
            initialize();
        }
        
        if (bookIterator.hasNext()) {
            return bookIterator.next();
        }
        
        return null; // End of data
    }
    
    private void initialize() {
        List<MongoBook> books = mongoTemplate.find(new Query(), MongoBook.class);
        bookIterator = books.iterator();
        initialized = true;
    }
    
    public void reset() {
        initialized = false;
        bookIterator = null;
    }
}