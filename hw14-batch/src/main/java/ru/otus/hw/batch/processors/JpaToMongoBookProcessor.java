package ru.otus.hw.batch.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.jpa.Book;
import ru.otus.hw.models.jpa.Genre;
import ru.otus.hw.models.mongo.MongoAuthor;
import ru.otus.hw.models.mongo.MongoBook;
import ru.otus.hw.models.mongo.MongoGenre;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class JpaToMongoBookProcessor implements ItemProcessor<Book, MongoBook> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaToMongoBookProcessor.class);

    @Override
    public MongoBook process(@NonNull Book jpaBook) {
        // Simple transformation: JPA Long ID -> MongoDB String ID
        String mongoId = String.valueOf(jpaBook.getId());
        
        // Transform author (embedded object - no complex relationships)
        MongoAuthor mongoAuthor = null;
        if (jpaBook.getAuthor() != null) {
            mongoAuthor = new MongoAuthor(
                String.valueOf(jpaBook.getAuthor().getId()),
                jpaBook.getAuthor().getFullName()
            );
        }
        
        // Transform genres (embedded objects - no complex relationships)
        List<MongoGenre> mongoGenres = null;
        if (jpaBook.getGenres() != null) {
            mongoGenres = jpaBook.getGenres().stream()
                .map(this::transformGenre)
                .collect(Collectors.toList());
        }
        
        MongoBook mongoBook = new MongoBook(mongoId, jpaBook.getTitle(), mongoAuthor, mongoGenres);
        
        LOGGER.debug("Transformed JPA Book ID: {} -> MongoDB Book ID: {}", 
                    jpaBook.getId(), mongoBook.getId());
        
        return mongoBook;
    }
    
    private MongoGenre transformGenre(Genre jpaGenre) {
        return new MongoGenre(
            String.valueOf(jpaGenre.getId()),
            jpaGenre.getName()
        );
    }
}