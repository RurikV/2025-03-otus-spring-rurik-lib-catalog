package ru.otus.hw.batch.processors;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
public class JpaToMongoBookProcessor implements ItemProcessor<Book, MongoBook> {

    @Override
    public MongoBook process(@NonNull Book jpaBook) {
        // Simple transformation: JPA Long ID -> MongoDB String ID
        String mongoId = String.valueOf(jpaBook.getId());
        
        MongoAuthor mongoAuthor = transformAuthor(jpaBook.getAuthor());
        List<MongoGenre> mongoGenres = transformGenres(jpaBook.getGenres());
        
        MongoBook mongoBook = new MongoBook(mongoId, jpaBook.getTitle(), mongoAuthor, mongoGenres);
        
        log.debug("Transformed JPA Book ID: {} -> MongoDB Book ID: {}", 
                    jpaBook.getId(), mongoBook.getId());
        
        return mongoBook;
    }
    
    private MongoAuthor transformAuthor(ru.otus.hw.models.jpa.Author jpaAuthor) {
        if (jpaAuthor == null) {
            return null;
        }
        return new MongoAuthor(
            String.valueOf(jpaAuthor.getId()),
            jpaAuthor.getFullName()
        );
    }
    
    private List<MongoGenre> transformGenres(List<Genre> jpaGenres) {
        if (jpaGenres == null) {
            return null;
        }
        return jpaGenres.stream()
            .map(this::transformGenre)
            .collect(Collectors.toList());
    }
    
    private MongoGenre transformGenre(Genre jpaGenre) {
        return new MongoGenre(
            String.valueOf(jpaGenre.getId()),
            jpaGenre.getName()
        );
    }
}