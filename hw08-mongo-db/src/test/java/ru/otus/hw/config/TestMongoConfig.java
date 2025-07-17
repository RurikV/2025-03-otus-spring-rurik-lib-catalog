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

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Import;
import java.util.concurrent.TimeUnit;

@TestConfiguration
@Import(MongoTestLifecycleManager.class)
public class TestMongoConfig {

    private MongoClient mongoClient;

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
        // Check if we're running in CI environment
        boolean isCI = System.getenv("CI") != null && System.getenv("CI").equals("true");

        if (isCI) {
            // In CI environment, use embedded MongoDB without authentication
            // Embedded MongoDB will be started automatically by Spring Boot with no_auth: true
            connectionString = String.format("mongodb://%s:%d/%s", host, port > 0 ? port : 27017, database);
            System.out.println("Using embedded MongoDB in CI environment (no auth): " + connectionString);
        } else if (!username.isEmpty() && !password.isEmpty()) {
            // Use the provided MongoDB credentials (external MongoDB service)
            connectionString = String.format("mongodb://%s:%s@%s:%d/%s?authSource=%s",
                    username, password, host, port, database, authDatabase);
            System.out.println("Using external MongoDB with provided credentials: " + connectionString);
        } else {
            // For local testing without credentials, try embedded MongoDB first, then external with defaults
            // Check if we can use embedded MongoDB (no credentials provided)
            if (username.isEmpty() && password.isEmpty()) {
                // Use embedded MongoDB without authentication for local testing
                connectionString = String.format("mongodb://%s:%d/%s", host, port > 0 ? port : 27017, database);
                System.out.println("Using embedded MongoDB for local testing (no auth): " + connectionString);
            } else {
                // Fallback to external MongoDB with default credentials
                String defaultUsername = "root";
                String defaultPassword = "example";
                int defaultPort = 27017;
                connectionString = String.format("mongodb://%s:%s@%s:%d/%s?authSource=%s",
                        defaultUsername, defaultPassword, host, defaultPort, database, authDatabase);
                System.out.println("Using external MongoDB for local testing: " + connectionString);
            }
        }

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .applyToSocketSettings(builder -> 
                    builder.connectTimeout(2000, TimeUnit.MILLISECONDS)
                           .readTimeout(2000, TimeUnit.MILLISECONDS))
                .build();
        this.mongoClient = MongoClients.create(settings);
        return this.mongoClient;
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

    @PreDestroy
    public void closeMongoClient() {
        if (mongoClient != null) {
            System.out.println("Closing MongoDB client connection...");
            mongoClient.close();
            System.out.println("MongoDB client connection closed.");
        }
    }
}
