package ru.otus.hw.batch.writers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.mongo.MongoAuthor;

import java.util.List;

@Slf4j
@Component
public class MongoAuthorItemWriter implements ItemWriter<MongoAuthor> {
    
    private final MongoTemplate mongoTemplate;
    
    @Autowired
    public MongoAuthorItemWriter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    @Override
    public void write(Chunk<? extends MongoAuthor> chunk) {
        List<? extends MongoAuthor> authors = chunk.getItems();
        
        // Simple direct save - no complex logic needed
        for (MongoAuthor author : authors) {
            mongoTemplate.save(author);
            log.debug("Saved MongoDB author: {} with ID: {}", author.getFullName(), author.getId());
        }
        
        log.debug("Saved {} authors to MongoDB", authors.size());
    }
}