package ru.otus.hw.batch.processors;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.otus.hw.mappers.EntityMapper;
import ru.otus.hw.models.jpa.Comment;
import ru.otus.hw.models.mongo.MongoComment;

@Component
public class CommentItemProcessor implements ItemProcessor<MongoComment, Comment> {
    
    private final EntityMapper entityMapper;
    
    @Autowired
    public CommentItemProcessor(EntityMapper entityMapper) {
        this.entityMapper = entityMapper;
    }
    
    @Override
    public Comment process(@NonNull MongoComment mongoComment) {
        // Transform MongoDB comment to JPA comment
        Comment comment = entityMapper.mapToComment(mongoComment);
        
        // Log the transformation for debugging
        System.out.println("[DEBUG_LOG] Processing comment: " + mongoComment.getText() + 
                          " for book ID: " + (comment.getBook() != null ? comment.getBook().getId() : "null"));
        
        return comment;
    }
}