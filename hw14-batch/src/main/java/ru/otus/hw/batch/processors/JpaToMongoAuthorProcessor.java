package ru.otus.hw.batch.processors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.jpa.Author;
import ru.otus.hw.models.mongo.MongoAuthor;

@Slf4j
@Component
public class JpaToMongoAuthorProcessor implements ItemProcessor<Author, MongoAuthor> {

    @Override
    public MongoAuthor process(@NonNull Author jpaAuthor) {
        // Simple transformation: JPA Long ID -> MongoDB String ID
        String mongoId = String.valueOf(jpaAuthor.getId());
        
        MongoAuthor mongoAuthor = new MongoAuthor(mongoId, jpaAuthor.getFullName());
        
        log.debug("Transformed JPA Author ID: {} -> MongoDB Author ID: {}", 
                    jpaAuthor.getId(), mongoAuthor.getId());
        
        return mongoAuthor;
    }
}