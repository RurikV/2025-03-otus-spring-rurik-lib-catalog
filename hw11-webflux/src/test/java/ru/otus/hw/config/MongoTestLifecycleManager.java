package ru.otus.hw.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

/**
 * Component responsible for managing MongoDB lifecycle during tests.
 * Ensures proper cleanup of MongoDB resources after each test context.
 */
@Slf4j
@TestComponent
public class MongoTestLifecycleManager {

    private final ApplicationContext applicationContext;

    public MongoTestLifecycleManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PreDestroy
    public void cleanup() {
        log.info("Starting MongoDB cleanup...");
        
        try {
            // Get ReactiveMongoTemplate from context if available and clear collections
            try {
                ReactiveMongoTemplate reactiveMongoTemplate = applicationContext.getBean(ReactiveMongoTemplate.class);
                log.info("Clearing MongoDB collections...");
                
                // Drop all collections using reactive approach
                reactiveMongoTemplate.getCollectionNames()
                    .flatMap(collectionName -> {
                        log.debug("Dropping collection: {}", collectionName);
                        return reactiveMongoTemplate.dropCollection(collectionName);
                    })
                    .doOnError(error -> log.warn("Failed to drop collection: {}", error.getMessage()))
                    .blockLast(); // Block to wait for completion in cleanup
                    
                log.info("MongoDB collections cleared successfully");
            } catch (Exception e) {
                log.debug("ReactiveMongoTemplate not available in context: {}", e.getMessage());
            }

            // Force garbage collection to help with resource cleanup
            System.gc();
            
            // Small delay to allow cleanup to complete
            Thread.sleep(500);
            
        } catch (Exception e) {
            log.error("Error during MongoDB cleanup: {}", e.getMessage(), e);
        }
        
        log.info("MongoDB cleanup completed");
    }
}