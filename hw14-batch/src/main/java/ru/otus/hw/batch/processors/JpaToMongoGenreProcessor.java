package ru.otus.hw.batch.processors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.jpa.Genre;
import ru.otus.hw.models.mongo.MongoGenre;

@Slf4j
@Component
public class JpaToMongoGenreProcessor implements ItemProcessor<Genre, MongoGenre> {

    @Override
    public MongoGenre process(@NonNull Genre jpaGenre) {
        // Simple transformation: JPA Long ID -> MongoDB String ID
        String mongoId = String.valueOf(jpaGenre.getId());
        
        MongoGenre mongoGenre = new MongoGenre(mongoId, jpaGenre.getName());
        
        log.debug("Transformed JPA Genre ID: {} -> MongoDB Genre ID: {}", 
                    jpaGenre.getId(), mongoGenre.getId());
        
        return mongoGenre;
    }
}