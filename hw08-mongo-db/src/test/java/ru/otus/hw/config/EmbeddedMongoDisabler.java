package ru.otus.hw.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;

/**
 * Configuration class that disables the embedded MongoDB when external MongoDB credentials are provided.
 * This prevents conflicts when running tests in environments with a real MongoDB instance.
 */
@TestConfiguration
@AutoConfigureBefore(EmbeddedMongoAutoConfiguration.class)
@ConditionalOnProperty(name = "spring.data.mongodb.username", matchIfMissing = false)
public class EmbeddedMongoDisabler {
    
    /**
     * This bean is a marker to indicate that embedded MongoDB should be disabled.
     * The presence of this bean along with the @ConditionalOnProperty annotation
     * will prevent the embedded MongoDB from starting when external MongoDB credentials are provided.
     */
    @Bean
    @Primary
    public boolean disableEmbeddedMongo() {
        return true;
    }
}