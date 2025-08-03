package ru.otus.hw.batch.writers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.mongo.MongoGenre;

import java.util.List;

@Slf4j
@Component
public class MongoGenreItemWriter implements ItemWriter<MongoGenre> {
    
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
            log.debug("Saved MongoDB genre: {} with ID: {}", genre.getName(), genre.getId());
        }
        
        log.debug("Saved {} genres to MongoDB", genres.size());
    }
}