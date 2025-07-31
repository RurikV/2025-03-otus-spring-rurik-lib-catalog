package ru.otus.hw.batch.writers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.mongo.MongoGenre;

import java.util.List;

@Component
public class MongoGenreItemWriter implements ItemWriter<MongoGenre> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoGenreItemWriter.class);
    
    private final MongoTemplate mongoTemplate;
    
    @Autowired
    public MongoGenreItemWriter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    @Override
    public void write(Chunk<? extends MongoGenre> chunk) {
        List<? extends MongoGenre> genres = chunk.getItems();
        
        // Simple direct save - no complex logic needed
        for (MongoGenre genre : genres) {
            mongoTemplate.save(genre);
            LOGGER.debug("Saved MongoDB genre: {} with ID: {}", genre.getName(), genre.getId());
        }
        
        LOGGER.debug("Saved {} genres to MongoDB", genres.size());
    }
}