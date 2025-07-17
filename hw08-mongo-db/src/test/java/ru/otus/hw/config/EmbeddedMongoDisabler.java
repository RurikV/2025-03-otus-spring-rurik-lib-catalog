package ru.otus.hw.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration;

/**
 * Configuration class that conditionally disables the embedded MongoDB.
 * This prevents conflicts when running tests with external MongoDB service.
 * Embedded MongoDB is disabled only when external MongoDB credentials are provided
 * (both username and password are non-empty). Otherwise, embedded MongoDB is allowed
 * to start, which is useful for CI environments without external MongoDB service.
 */
@TestConfiguration
@EnableAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
@ConditionalOnProperty(name = {"spring.data.mongodb.username", "spring.data.mongodb.password"}, matchIfMissing = false)
public class EmbeddedMongoDisabler {
    // This class disables embedded MongoDB by excluding EmbeddedMongoAutoConfiguration
    // Only when external MongoDB credentials are provided
    // No additional beans needed - the exclusion is handled by the annotation
}
