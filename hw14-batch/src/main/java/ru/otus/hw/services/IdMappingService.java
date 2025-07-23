package ru.otus.hw.services;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class IdMappingService {
    
    private final ConcurrentHashMap<String, Long> authorIdMapping = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Long> genreIdMapping = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, Long> bookIdMapping = new ConcurrentHashMap<>();
    
    private final AtomicLong authorIdCounter = new AtomicLong(1);

    private final AtomicLong genreIdCounter = new AtomicLong(1);

    private final AtomicLong bookIdCounter = new AtomicLong(1);
    
    public Long getOrCreateAuthorId(String mongoId) {
        return authorIdMapping.computeIfAbsent(mongoId, k -> authorIdCounter.getAndIncrement());
    }
    
    public Long getOrCreateGenreId(String mongoId) {
        return genreIdMapping.computeIfAbsent(mongoId, k -> genreIdCounter.getAndIncrement());
    }
    
    public Long getOrCreateBookId(String mongoId) {
        return bookIdMapping.computeIfAbsent(mongoId, k -> bookIdCounter.getAndIncrement());
    }
    
    public Long getBookId(String mongoId) {
        return bookIdMapping.get(mongoId);
    }
    
    public void clearMappings() {
        authorIdMapping.clear();
        genreIdMapping.clear();
        bookIdMapping.clear();
        authorIdCounter.set(1);
        genreIdCounter.set(1);
        bookIdCounter.set(1);
    }
}