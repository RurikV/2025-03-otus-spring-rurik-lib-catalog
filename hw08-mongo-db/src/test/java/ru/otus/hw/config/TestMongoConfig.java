package ru.otus.hw.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.concurrent.TimeUnit;

@TestConfiguration
public class TestMongoConfig {

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Bean
    @Primary
    public MongoClient mongoClient() {
        // For embedded MongoDB tests, we don't need to specify a connection string
        // Spring Boot will automatically configure the embedded MongoDB
        // and the tests will use it
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToSocketSettings(builder -> 
                    builder.connectTimeout(1000, TimeUnit.MILLISECONDS)
                           .readTimeout(1000, TimeUnit.MILLISECONDS))
                .build();
        return MongoClients.create(settings);
    }

    @Bean
    @Primary
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, database);
    }

    @Bean
    @Primary
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        return new MongoTemplate(mongoDatabaseFactory);
    }
}
