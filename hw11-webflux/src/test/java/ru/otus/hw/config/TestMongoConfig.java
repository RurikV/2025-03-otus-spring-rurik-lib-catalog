package ru.otus.hw.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Simple MongoDB test configuration that works with reactive MongoDB.
 * Relies on Spring Boot's auto-configuration for embedded MongoDB in tests.
 */
@TestConfiguration
@Import(MongoTestLifecycleManager.class)
public class TestMongoConfig {
    // This configuration relies on Spring Boot's auto-configuration
    // for reactive MongoDB with embedded MongoDB support.
    // The embedded MongoDB will be automatically configured by Spring Boot
    // when running tests with the de.flapdoodle.embed.mongo dependency.
}