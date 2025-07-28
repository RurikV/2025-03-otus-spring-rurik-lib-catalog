package ru.otus.hw.batch.writers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.mongo.MongoBook;

import java.util.List;

@Component
public class MongoBookItemWriter implements ItemWriter<MongoBook> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoBookItemWriter.class);
    
    private final MongoTemplate mongoTemplate;
    
    @Autowired
    public MongoBookItemWriter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    @Override
    public void write(Chunk<? extends MongoBook> chunk) {
        List<? extends MongoBook> books = chunk.getItems();
        
        // Simple direct save - no complex logic needed
        for (MongoBook book : books) {
            mongoTemplate.save(book);
            LOGGER.debug("Saved MongoDB book: {} with ID: {}", book.getTitle(), book.getId());
        }
        
        LOGGER.debug("Saved {} books to MongoDB", books.size());
    }
}