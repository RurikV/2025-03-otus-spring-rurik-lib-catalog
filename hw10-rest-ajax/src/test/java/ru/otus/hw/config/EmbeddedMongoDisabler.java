package ru.otus.hw.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.test.context.TestConfiguration;
import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration;

/**
 * Configuration class that conditionally disables the embedded MongoDB.
 * This prevents conflicts when running tests with external MongoDB service.
 * Embedded MongoDB is disabled when NOT in CI environment, allowing external MongoDB to be used.
 * In CI environment, embedded MongoDB is allowed to start automatically.
 */
@TestConfiguration
@EnableAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
@ConditionalOnExpression("'${CI:}'.isEmpty() or '${CI:}' != 'true'")
public class EmbeddedMongoDisabler {
    // This class disables embedded MongoDB by excluding EmbeddedMongoAutoConfiguration
    // Activates for local testing (when CI is not set or CI=false) to use external MongoDB
    // Does not activate in CI environment (when CI=true) to allow embedded MongoDB
    // No additional beans needed - the exclusion is handled by the annotation
}
