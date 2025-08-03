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
import ru.otus.hw.models.jpa.Genre;
import ru.otus.hw.repositories.GenreRepository;

import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
@Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class JpaGenreItemReader implements ItemReader<Genre> {
    
    private final GenreRepository genreRepository;

    private Iterator<Genre> genreIterator;

    private boolean initialized = false;
    
    @Autowired
    public JpaGenreItemReader(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }
    
    @Override
    public Genre read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!initialized) {
            initialize();
        }
        
        if (genreIterator != null && genreIterator.hasNext()) {
            return genreIterator.next();
        }
        
        return null; // End of data
    }
    
    public void reset() {
        initialized = false;
        genreIterator = null;
    }
    
    private void initialize() {
        // Use only findAll operation - no complex queries
        List<Genre> genres = genreRepository.findAll();
        genreIterator = genres.iterator();
        initialized = true;
        log.debug("JpaGenreItemReader initialized with {} genres", genres.size());
    }
}