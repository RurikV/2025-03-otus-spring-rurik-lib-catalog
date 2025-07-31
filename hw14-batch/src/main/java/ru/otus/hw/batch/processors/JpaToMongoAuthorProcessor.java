package ru.otus.hw.batch.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.jpa.Author;
import ru.otus.hw.models.mongo.MongoAuthor;

@Component
public class JpaToMongoAuthorProcessor implements ItemProcessor<Author, MongoAuthor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaToMongoAuthorProcessor.class);

    @Override
    public MongoAuthor process(@NonNull Author jpaAuthor) {
        // Simple transformation: JPA Long ID -> MongoDB String ID
        String mongoId = String.valueOf(jpaAuthor.getId());
        
        MongoAuthor mongoAuthor = new MongoAuthor(mongoId, jpaAuthor.getFullName());
        
        LOGGER.debug("Transformed JPA Author ID: {} -> MongoDB Author ID: {}", 
                    jpaAuthor.getId(), mongoAuthor.getId());
        
        return mongoAuthor;
    }
}