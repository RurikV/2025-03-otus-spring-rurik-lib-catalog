package ru.otus.hw.batch.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.jpa.Comment;
import ru.otus.hw.models.mongo.MongoBook;
import ru.otus.hw.models.mongo.MongoComment;

@Component
public class JpaToMongoCommentProcessor implements ItemProcessor<Comment, MongoComment> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaToMongoCommentProcessor.class);

    @Override
    public MongoComment process(@NonNull Comment jpaComment) {
        // Simple transformation: JPA Long ID -> MongoDB String ID
        String mongoId = String.valueOf(jpaComment.getId());
        
        // Transform book reference - create simple reference object
        MongoBook mongoBook = null;
        if (jpaComment.getBook() != null) {
            // Create a simple book reference with just ID and title
            mongoBook = new MongoBook(
                String.valueOf(jpaComment.getBook().getId()),
                jpaComment.getBook().getTitle(),
                null, // No need for full author/genres in reference
                null
            );
        }
        
        MongoComment mongoComment = new MongoComment(mongoId, jpaComment.getText(), mongoBook);
        
        LOGGER.debug("Transformed JPA Comment ID: {} -> MongoDB Comment ID: {}", 
                    jpaComment.getId(), mongoComment.getId());
        
        return mongoComment;
    }
}