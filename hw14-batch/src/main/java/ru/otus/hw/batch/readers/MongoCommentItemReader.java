package ru.otus.hw.batch.readers;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.mongo.MongoComment;

import java.util.Iterator;
import java.util.List;

@Component
@Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MongoCommentItemReader implements ItemReader<MongoComment> {
    
    private final MongoTemplate mongoTemplate;

    private Iterator<MongoComment> commentIterator;

    private boolean initialized = false;
    
    @Autowired
    public MongoCommentItemReader(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    @Override
    public MongoComment read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
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
        List<MongoComment> comments = mongoTemplate.find(new Query(), MongoComment.class);
        commentIterator = comments.iterator();
        initialized = true;
        System.out.println("[DEBUG_LOG] MongoCommentItemReader initialized with " + comments.size() + " comments");
    }
}