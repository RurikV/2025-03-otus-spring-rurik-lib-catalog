package ru.otus.hw.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration;

/**
 * Configuration class that disables the embedded MongoDB in all test environments.
 * This prevents conflicts when running tests since we use external MongoDB via TestMongoConfig.
 * This configuration excludes EmbeddedMongoAutoConfiguration to ensure embedded MongoDB
 * never starts, allowing us to use external MongoDB consistently.
 */
@TestConfiguration
@EnableAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
public class EmbeddedMongoDisabler {
    // This class disables embedded MongoDB by excluding EmbeddedMongoAutoConfiguration
    // No additional beans needed - the exclusion is handled by the annotation
}
