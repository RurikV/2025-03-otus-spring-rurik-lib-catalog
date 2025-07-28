package ru.otus.hw.batch.writers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.jpa.Comment;
import ru.otus.hw.repositories.CommentRepository;

import java.util.List;

@Component
public class CommentItemWriter implements ItemWriter<Comment> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentItemWriter.class);
    
    private final CommentRepository commentRepository;
    
    @Autowired
    public CommentItemWriter(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }
    
    @Override
    public void write(Chunk<? extends Comment> chunk) {
        List<? extends Comment> comments = chunk.getItems();
        
        for (Comment comment : comments) {
            // Check if comment already exists
            Comment existingComment = null;
            if (comment.getBook() != null) {
                existingComment = commentRepository.findByTextAndBook(comment.getText(), comment.getBook());
            }
            
            if (existingComment != null) {
                // Comment already exists, skip saving
                LOGGER.debug("Found existing comment: {} for book ID: {}", 
                           existingComment.getText(), existingComment.getBook().getId());
            } else {
                // Save new comment
                commentRepository.save(comment);
                LOGGER.debug("Saved comment: {} for book ID: {}", comment.getText(), 
                           comment.getBook() != null ? comment.getBook().getId() : "null");
            }
        }
    }
}