# Spring Batch Migration - Library Catalog

A Spring Batch application that migrates library catalog data from MongoDB to H2 (JPA) database using Spring Boot and Spring Shell.

## Project Description

This application demonstrates data migration using Spring Batch framework. It migrates a library catalog containing authors, books, genres, and comments from a MongoDB database to an H2 relational database. The application provides an interactive shell interface for controlling the migration process.

### Features

- **Data Migration**: Migrates authors, genres, books, and comments from MongoDB to H2 database
- **Interactive Shell**: Command-line interface for controlling migration operations
- **Test Data**: Automatically loads sample library data into MongoDB on startup
- **ID Mapping**: Maintains mapping between MongoDB ObjectIds and JPA generated IDs
- **Restart Capability**: Supports restarting migration with cache clearing
- **Debug Logging**: Comprehensive logging for monitoring migration progress

## Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **MongoDB** running on localhost:27017 (default configuration)

## Setup Instructions

### 1. Clone and Navigate to Project

```bash
cd hw14-batch
```

### 2. Start MongoDB

**Important**: This application is configured to connect to MongoDB without authentication by default. Choose one of the following options:

#### Option A: MongoDB without Authentication (Recommended for Development)

```bash
# Using MongoDB service (Linux/macOS)
sudo systemctl start mongod
# or
brew services start mongodb/brew/mongodb-community

# Using Docker without authentication
docker run -d -p 27017:27017 --name mongodb mongo:latest

# Using Docker Compose (create docker-compose.yml)
version: '3.8'
services:
  mongodb:
    image: mongo:latest
    ports:
      - "27017:27017"
    environment:
      - MONGO_INITDB_DATABASE=library
```

#### Option B: MongoDB with Authentication

If your MongoDB requires authentication, update `src/main/resources/application.yml`:

```yaml
spring:
  data:
    mongodb:
      # Replace the default uri with authenticated connection
      uri: mongodb://username:password@localhost:27017/library?authSource=admin
```

```bash
# Using Docker with authentication
docker run -d -p 27017:27017 --name mongodb \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=password \
  mongo:latest
```

### 3. Build the Project

```bash
mvn clean compile
```

## How to Run

### Option 1: Using Maven (Recommended)

```bash
mvn spring-boot:run
```

### Option 2: Using JAR file

```bash
# Build the JAR
mvn clean package

# Run the JAR
java -jar target/hw14-batch-0.0.1-SNAPSHOT.jar
```

## Usage

Once the application starts, you'll see a Spring Shell prompt. The application will automatically:

1. Load test data into MongoDB (authors, genres, books, comments)
2. Start the interactive shell

### Available Commands

| Command | Aliases | Description |
|---------|---------|-------------|
| `migrate` | `start-migration` | Start the migration job |
| `restart` | `restart-migration` | Restart migration with cleared cache |
| `clear` | `clear-cache` | Clear ID mappings cache |
| `status` | `migration-status` | Show migration status |
| `help` | | Show all available commands |
| `exit` | | Exit the application |

### Example Usage

```bash
# Check status
shell:>status
Migration utility is ready. Use 'migrate' to start migration or 'restart' to restart with cleared cache.

# Start migration
shell:>migrate
Migration job started with execution ID: 1, Status: STARTED

# Restart migration (clears cache first)
shell:>restart
[DEBUG_LOG] Cleared ID mappings for restart
Migration job started with execution ID: 2, Status: STARTED

# Clear cache manually
shell:>clear
ID mappings cache cleared successfully

# Exit application
shell:>exit
```

## Configuration

### Database Configuration

The application uses the following default configurations:

**MongoDB** (Source):
- Host: localhost
- Port: 27017
- Database: library

**H2 Database** (Target):
- URL: jdbc:h2:mem:testdb
- Username: sa
- Password: password
- Console: http://localhost:8080/h2-console (enabled)

### Customizing Configuration

You can override default settings by modifying `src/main/resources/application.yml` or using environment variables:

```yaml
spring:
  data:
    mongodb:
      host: your-mongodb-host
      port: 27017
      database: your-database-name
  
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password: password
```

## Test Data

The application automatically loads the following test data into MongoDB:

**Authors:**
- Leo Tolstoy
- Fyodor Dostoevsky
- Alexander Pushkin

**Genres:**
- Classic Literature
- Philosophy
- Romance
- Drama

**Books:**
- War and Peace (Tolstoy)
- Crime and Punishment (Dostoevsky)
- Eugene Onegin (Pushkin)
- Anna Karenina (Tolstoy)

**Comments:**
- 5 sample comments on various books

## Monitoring Migration

### Logs
The application provides detailed logging. Look for:
- `[DEBUG_LOG]` prefixed messages for migration progress
- Spring Batch execution details
- Database operation logs

### H2 Console
Access the H2 database console at http://localhost:8080/h2-console to verify migrated data:
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

## Troubleshooting

### Common Issues

1. **MongoDB Authentication Error**
   ```
   Command failed with error 13 (Unauthorized): 'Command find requires authentication'
   ```
   **Solution**: Your MongoDB instance requires authentication but the application is configured for no authentication.
   
   **Option 1 - Disable MongoDB Authentication (Recommended for Development)**:
   ```bash
   # Stop MongoDB
   sudo systemctl stop mongod
   
   # Edit MongoDB config file
   sudo nano /etc/mongod.conf
   
   # Comment out or remove the security section:
   # security:
   #   authorization: enabled
   
   # Restart MongoDB
   sudo systemctl start mongod
   ```
   
   **Option 2 - Configure Application for Authentication**:
   Update `application.yml`:
   ```yaml
   spring:
     data:
       mongodb:
         uri: mongodb://your-username:your-password@localhost:27017/library?authSource=admin
   ```

2. **MongoDB Connection Failed**
   - Ensure MongoDB is running on localhost:27017
   - Check MongoDB service status: `sudo systemctl status mongod`
   - Verify MongoDB is accessible: `mongo --host localhost --port 27017`

3. **Port Already in Use**
   - Change the server port in application.yml:
     ```yaml
     server:
       port: 8081
     ```

4. **Migration Job Fails**
   - Use `restart` command to clear cache and retry
   - Check logs for specific error messages
   - Verify MongoDB contains the expected test data
   - Ensure both MongoDB and H2 databases are accessible

### Debug Mode

The application runs with debug logging enabled by default. To reduce log verbosity, modify `application.yml`:

```yaml
logging:
  level:
    org.springframework.batch: INFO
    ru.otus.hw: INFO
```

## Development

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=MigrationJobIntegrationTest
```

### Project Structure

```
src/
├── main/
│   ├── java/ru/otus/hw/
│   │   ├── BatchMigrationApplication.java    # Main application class
│   │   ├── config/                           # Configuration classes
│   │   ├── models/                           # JPA and MongoDB models
│   │   ├── repositories/                     # Data repositories
│   │   ├── services/                         # Business logic
│   │   └── shell/                           # Shell commands
│   └── resources/
│       └── application.yml                   # Application configuration
└── test/                                     # Test classes
```

## License

This project is part of the OTUS Spring Framework course.