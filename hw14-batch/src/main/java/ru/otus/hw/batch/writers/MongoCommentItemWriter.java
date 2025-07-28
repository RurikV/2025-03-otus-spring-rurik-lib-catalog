package ru.otus.hw.batch.writers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.mongo.MongoComment;

import java.util.List;

@Component
public class MongoCommentItemWriter implements ItemWriter<MongoComment> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoCommentItemWriter.class);
    
    private final MongoTemplate mongoTemplate;
    
    @Autowired
    public MongoCommentItemWriter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    @Override
    public void write(Chunk<? extends MongoComment> chunk) {
        List<? extends MongoComment> comments = chunk.getItems();
        
        // Simple direct save - no complex logic needed
        for (MongoComment comment : comments) {
            mongoTemplate.save(comment);
            LOGGER.debug("Saved MongoDB comment: {} with ID: {}", comment.getText(), comment.getId());
        }
        
        LOGGER.debug("Saved {} comments to MongoDB", comments.size());
    }
}