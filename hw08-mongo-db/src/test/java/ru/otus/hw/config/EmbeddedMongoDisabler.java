package ru.otus.hw.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * Configuration class that disables the embedded MongoDB when running in CI environment
 * or when an external MongoDB instance is available.
 * This prevents conflicts when running tests in environments with a real MongoDB instance.
 * 
 * The configuration is active when:
 * - The environment variable CI=true is set (GitHub Actions)
 * - Or when running locally with external MongoDB (default behavior)
 */
@TestConfiguration
@EnableAutoConfiguration(exclude = {EmbeddedMongoAutoConfiguration.class})
public class EmbeddedMongoDisabler {

    /**
     * This bean is a marker to indicate that embedded MongoDB has been disabled.
     * The @EnableAutoConfiguration(exclude = {EmbeddedMongoAutoConfiguration.class})
     * annotation ensures that embedded MongoDB will not start at all.
     */
    @Bean
    @Primary
    public boolean disableEmbeddedMongo() {
        // Log that we're disabling embedded MongoDB
        boolean isCI = System.getenv("CI") != null && System.getenv("CI").equals("true");
        if (isCI) {
            System.out.println("Disabling embedded MongoDB for CI environment");
        } else {
            System.out.println("Disabling embedded MongoDB for local testing with external MongoDB");
        }
        return true;
    }
}
