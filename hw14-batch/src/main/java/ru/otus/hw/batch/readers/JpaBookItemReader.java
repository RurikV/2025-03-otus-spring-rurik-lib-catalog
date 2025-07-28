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
import ru.otus.hw.models.jpa.Book;
import ru.otus.hw.repositories.BookRepository;

import java.util.Iterator;
import java.util.List;

@Component
@Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class JpaBookItemReader implements ItemReader<Book> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaBookItemReader.class);
    
    private final BookRepository bookRepository;

    private Iterator<Book> bookIterator;

    private boolean initialized = false;
    
    @Autowired
    public JpaBookItemReader(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }
    
    @Override
    public Book read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!initialized) {
            initialize();
        }
        
        if (bookIterator != null && bookIterator.hasNext()) {
            return bookIterator.next();
        }
        
        return null; // End of data
    }
    
    public void reset() {
        initialized = false;
        bookIterator = null;
    }
    
    private void initialize() {
        // Use only findAll operation - no complex queries
        List<Book> books = bookRepository.findAll();
        bookIterator = books.iterator();
        initialized = true;
        LOGGER.debug("JpaBookItemReader initialized with {} books", books.size());
    }
}