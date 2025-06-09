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

    @Value("${spring.data.mongodb.port:0}")
    private int port;

    @Value("${spring.data.mongodb.host:localhost}")
    private String host;

    @Value("${spring.data.mongodb.username:}")
    private String username;

    @Value("${spring.data.mongodb.password:}")
    private String password;

    @Value("${spring.data.mongodb.authentication-database:admin}")
    private String authDatabase;

    @Bean
    @Primary
    public MongoClient mongoClient() {
        // Use the embedded MongoDB with a random port
        // or the MongoDB service container if specified in the environment
        String connectionString;
        if (!username.isEmpty() && !password.isEmpty()) {
            connectionString = String.format("mongodb://%s:%s@%s:%d/%s?authSource=%s",
                    username, password, host, port, database, authDatabase);
        } else {
            connectionString = String.format("mongodb://%s:%d", host, port);
        }
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
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
