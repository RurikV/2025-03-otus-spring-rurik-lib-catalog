# hw16-actuator

Production-grade observability demo using Spring Boot Actuator + Spring Data REST (HATEOAS).

What’s included:
- Spring Boot Actuator with exposed endpoints: health, info, metrics, logfile
- Custom HealthIndicator: reports current number of books (books.count)
- Spring Data REST with base path `/api`
- Tests: unit test for HealthIndicator and integration test for actuator endpoints

## Prerequisites
- Java 17+
- Maven 3.9+
- Docker (optional, to run MongoDB locally)

## Run MongoDB locally (Docker)
Application configuration expects MongoDB credentials `root/example` and database `library`:

```bash
# Start MongoDB
docker run --rm -d \
  --name mongo-lib \
  -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=root \
  -e MONGO_INITDB_ROOT_PASSWORD=example \
  mongo:6
```

## Build and run the app (module only)
From the repository root:

```bash
# Build only this module and its dependencies
mvn -pl hw16-actuator -am clean package

# Run the application
mvn -pl hw16-actuator spring-boot:run
```

The app will start on http://localhost:8080.

## Actuator endpoints
- Health: http://localhost:8080/actuator/health
  - Contains custom `library` component with `books.count` detail
- Metrics: http://localhost:8080/actuator/metrics
- Logfile: http://localhost:8080/actuator/logfile
  - Returns content of the file configured at `logging.file.name` (see below)

Actuator is configured in `src/main/resources/application.yml`:
- management.endpoints.web.exposure.include: `health,info,metrics,logfile`
- management.endpoint.health.show-details: `always`

## Log file
Log file path is configured as:
```
logging.file.name=logs/hw16-actuator.log
```
The logfile actuator endpoint returns contents of that file. Ensure the `logs` directory is writable by the process.

## Spring Data REST
All repository resources are exposed under base path `/api`. Examples:
- Authors: `GET http://localhost:8080/api/authors`
- Books:   `GET http://localhost:8080/api/books`
- Genres:  `GET http://localhost:8080/api/genres`

## Running tests
From the repository root:

```bash
# Run tests for this module only
mvn -pl hw16-actuator test
```

What the tests cover:
- `LibraryHealthIndicatorTest`: unit test for custom health indicator
- `ActuatorIntegrationTest`: starts a test context (with Mongo auto-config disabled),
  verifies `health`, `metrics`, and `logfile` actuator endpoints

Notes:
- Tests run under Spring profile `test`. DB seeding (`MongoConfig.initializeDatabase`) is disabled for tests via `@Profile("!test")`.
- `ActuatorIntegrationTest` mocks Spring Data repositories/services and writes a line into `logs/hw16-actuator.log` to validate `/actuator/logfile`.

## Stop MongoDB (if started via Docker)
```bash
docker stop mongo-lib
```
