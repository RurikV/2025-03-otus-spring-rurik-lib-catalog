package ru.otus.hw.batch.readers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.jpa.Comment;
import ru.otus.hw.repositories.CommentRepository;

import java.util.Iterator;
import java.util.List;

@Component
@Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class JpaCommentItemReader implements ItemReader<Comment> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaCommentItemReader.class);
    
    private final CommentRepository commentRepository;

    private Iterator<Comment> commentIterator;

    private boolean initialized = false;
    
    @Autowired
    public JpaCommentItemReader(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }
    
    @Override
    public Comment read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!initialized) {
            initialize();
        }
        
        if (commentIterator != null && commentIterator.hasNext()) {
            return commentIterator.next();
        }
        
        return null; // End of data
    }
    
    public void reset() {
        initialized = false;
        commentIterator = null;
    }
    
    private void initialize() {
        // Use only findAll operation - no complex queries
        List<Comment> comments = commentRepository.findAll();
        commentIterator = comments.iterator();
        initialized = true;
        LOGGER.debug("JpaCommentItemReader initialized with {} comments", comments.size());
    }
}