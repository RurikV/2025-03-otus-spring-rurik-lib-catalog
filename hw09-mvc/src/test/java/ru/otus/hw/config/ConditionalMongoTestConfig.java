package ru.otus.hw.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Simple MongoDB test configuration that works for both CI and local environments.
 * Uses embedded MongoDB with no authentication for simplicity.
 */
@TestConfiguration
@Import(MongoTestLifecycleManager.class)
public class ConditionalMongoTestConfig {
    // Simple configuration that relies on embedded MongoDB auto-configuration
    // with no_auth: true setting in application.yml
}