package ru.otus.hw.batch.writers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.mongo.MongoBook;

import java.util.List;

@Slf4j
@Component
public class MongoBookItemWriter implements ItemWriter<MongoBook> {
    
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
            log.debug("Saved MongoDB book: {} with ID: {}", book.getTitle(), book.getId());
        }
        
        log.debug("Saved {} books to MongoDB", books.size());
    }
}