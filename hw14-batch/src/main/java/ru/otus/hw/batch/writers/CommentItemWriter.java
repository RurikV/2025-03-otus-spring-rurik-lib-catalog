package ru.otus.hw.batch.writers;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.jpa.Comment;
import ru.otus.hw.repositories.CommentRepository;

import java.util.List;

@Component
public class CommentItemWriter implements ItemWriter<Comment> {
    
    private final CommentRepository commentRepository;
    
    @Autowired
    public CommentItemWriter(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }
    
    @Override
    public void write(Chunk<? extends Comment> chunk) {
        List<? extends Comment> comments = chunk.getItems();
        
        for (Comment comment : comments) {
            commentRepository.save(comment);
            System.out.println("[DEBUG_LOG] Saved comment: " + comment.getText() + 
                              " for book ID: " + (comment.getBook() != null ? comment.getBook().getId() : "null"));
        }
    }
}