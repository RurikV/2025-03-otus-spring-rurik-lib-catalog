package ru.otus.hw.config;

import com.mongodb.client.MongoClient;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;

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
            // Get MongoTemplate from context if available and clear collections
            try {
                MongoTemplate mongoTemplate = applicationContext.getBean(MongoTemplate.class);
                log.info("Clearing MongoDB collections...");
                final MongoTemplate finalMongoTemplate = mongoTemplate;
                mongoTemplate.getCollectionNames().forEach(collectionName -> {
                    try {
                        finalMongoTemplate.dropCollection(collectionName);
                        log.debug("Dropped collection: {}", collectionName);
                    } catch (Exception e) {
                        log.warn("Failed to drop collection {}: {}", collectionName, e.getMessage());
                    }
                });
            } catch (Exception e) {
                log.debug("MongoTemplate not available in context: {}", e.getMessage());
            }

            // Get MongoClient from context if available
            MongoClient mongoClient = null;
            try {
                mongoClient = applicationContext.getBean(MongoClient.class);
            } catch (Exception e) {
                log.debug("MongoClient not available in context: {}", e.getMessage());
            }

            // Close MongoClient if available
            if (mongoClient != null) {
                log.info("Closing MongoDB client...");
                mongoClient.close();
                log.info("MongoDB client closed successfully");
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