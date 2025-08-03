package ru.otus.hw.batch.readers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.jpa.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
@Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class JpaAuthorItemReader implements ItemReader<Author> {
    
    private final AuthorRepository authorRepository;

    private Iterator<Author> authorIterator;

    private boolean initialized = false;
    
    @Autowired
    public JpaAuthorItemReader(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }
    
    @Override
    public Author read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!initialized) {
            initialize();
        }
        
        if (authorIterator != null && authorIterator.hasNext()) {
            return authorIterator.next();
        }
        
        return null; // End of data
    }
    
    public void reset() {
        initialized = false;
        authorIterator = null;
    }
    
    private void initialize() {
        // Use only findAll operation - no complex queries
        List<Author> authors = authorRepository.findAll();
        authorIterator = authors.iterator();
        initialized = true;
        log.debug("JpaAuthorItemReader initialized with {} authors", authors.size());
    }
}