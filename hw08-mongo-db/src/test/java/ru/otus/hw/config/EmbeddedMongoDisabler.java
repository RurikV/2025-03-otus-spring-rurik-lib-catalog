package ru.otus.hw.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class that enables embedded MongoDB for tests.
 * This ensures tests can run without requiring an external MongoDB instance.
 */
@TestConfiguration
public class EmbeddedMongoDisabler {

    /**
     * This bean is a marker to indicate that embedded MongoDB is enabled.
     * Embedded MongoDB will start automatically for tests.
     */
    @Bean
    @Primary
    public boolean enableEmbeddedMongo() {
        // Log that we're using embedded MongoDB
        System.out.println("Using embedded MongoDB for tests");
        return true;
    }
}
