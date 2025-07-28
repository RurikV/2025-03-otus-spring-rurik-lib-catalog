package ru.otus.hw.batch.readers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.mongo.MongoBook;

import java.util.Iterator;
import java.util.List;

@Component
@Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MongoBookItemReader implements ItemReader<MongoBook> {
    
    private static final Logger logger = LoggerFactory.getLogger(MongoBookItemReader.class);
    
    private final MongoTemplate mongoTemplate;

    private Iterator<MongoBook> bookIterator;

    private boolean initialized = false;
    
    @Autowired
    public MongoBookItemReader(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    @Override
    public MongoBook read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!initialized) {
            initialize();
        }
        
        if (bookIterator != null && bookIterator.hasNext()) {
            return bookIterator.next();
        }
        
        return null; // End of data
    }
    
    public void reset() {
        initialized = false;
        bookIterator = null;
    }
    
    private void initialize() {
        List<MongoBook> books = mongoTemplate.find(new Query(), MongoBook.class);
        bookIterator = books.iterator();
        initialized = true;
        logger.debug("MongoBookItemReader initialized with {} books", books.size());
    }
}