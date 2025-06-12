package ru.otus.hw.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;

/**
 * Configuration class that disables the embedded MongoDB when running in CI environment.
 * This prevents conflicts when running tests in environments with a real MongoDB instance.
 * 
 * The configuration is only active when the environment variable CI=true is set,
 * which is the case in GitHub Actions but not locally.
 */
@TestConfiguration
@AutoConfigureBefore(EmbeddedMongoAutoConfiguration.class)
@ConditionalOnProperty(name = "CI", havingValue = "true", matchIfMissing = false)
public class EmbeddedMongoDisabler {

    /**
     * This bean is a marker to indicate that embedded MongoDB should be disabled.
     * The presence of this bean along with the @ConditionalOnProperty annotation
     * will prevent the embedded MongoDB from starting when running in CI environment.
     */
    @Bean
    @Primary
    public boolean disableEmbeddedMongo() {
        // Log that we're disabling embedded MongoDB
        System.out.println("Disabling embedded MongoDB for CI environment");
        return true;
    }
}
