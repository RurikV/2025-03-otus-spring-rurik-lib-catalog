package ru.otus.hw.batch.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.jpa.Genre;
import ru.otus.hw.models.mongo.MongoGenre;

@Component
public class JpaToMongoGenreProcessor implements ItemProcessor<Genre, MongoGenre> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaToMongoGenreProcessor.class);

    @Override
    public MongoGenre process(@NonNull Genre jpaGenre) {
        // Simple transformation: JPA Long ID -> MongoDB String ID
        String mongoId = String.valueOf(jpaGenre.getId());
        
        MongoGenre mongoGenre = new MongoGenre(mongoId, jpaGenre.getName());
        
        LOGGER.debug("Transformed JPA Genre ID: {} -> MongoDB Genre ID: {}", 
                    jpaGenre.getId(), mongoGenre.getId());
        
        return mongoGenre;
    }
}