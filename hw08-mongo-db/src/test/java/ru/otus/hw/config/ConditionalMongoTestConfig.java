package ru.otus.hw.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration;

/**
 * Conditional MongoDB test configuration that handles both CI and local environments.
 * - In CI environment (CI=true): Allows embedded MongoDB to start automatically
 * - In local environment (CI not set or CI!=true): Disables embedded MongoDB to use external MongoDB
 */
@TestConfiguration
public class ConditionalMongoTestConfig {

    /**
     * Configuration that disables embedded MongoDB for local testing.
     * Only activates when NOT in CI environment.
     */
    @TestConfiguration
    @EnableAutoConfiguration(exclude = EmbeddedMongoAutoConfiguration.class)
    @ConditionalOnExpression("'${CI:}'.isEmpty() or '${CI:}' != 'true'")
    @Import({TestMongoConfig.class, MongoTestLifecycleManager.class})
    public static class LocalMongoConfig {
        // Disables embedded MongoDB for local testing with external MongoDB
    }

    /**
     * Configuration that allows embedded MongoDB for CI environment.
     * Only activates when in CI environment.
     */
    @TestConfiguration
    @ConditionalOnExpression("'${CI:}' == 'true'")
    @Import({TestMongoConfig.class, MongoTestLifecycleManager.class})
    public static class CIMongoConfig {
        // Allows embedded MongoDB to start automatically in CI
    }
}