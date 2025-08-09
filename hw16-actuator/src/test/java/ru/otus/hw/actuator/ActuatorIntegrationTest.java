package ru.otus.hw.actuator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                // Avoid real Mongo autoconfiguration
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration",
                // Ensure details in health response
                "management.endpoint.health.show-details=always",
                // Expose required actuator endpoints explicitly for test context
                "management.endpoints.web.exposure.include=health,info,metrics,logfile",
                // Ensure logfile endpoint has a target file
                "logging.file.name=logs/hw16-actuator.log"
        })
@ActiveProfiles("test")
@DisplayName("Actuator endpoints")
class ActuatorIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private BookRepository bookRepository;

    // Mock other repos/services to satisfy component wiring without real Mongo
    @MockBean private AuthorRepository authorRepository;
    @MockBean private GenreRepository genreRepository;
    @MockBean private CommentRepository commentRepository;

    @MockBean private AuthorService authorService;
    @MockBean private BookService bookService;
    @MockBean private CommentService commentService;
    @MockBean private GenreService genreService;

    private Path logFilePath;

    @BeforeEach
    void setUp() throws Exception {
        // Prepare logfile with a test line to be returned by /actuator/logfile
        Path logsDir = Path.of("logs");
        Files.createDirectories(logsDir);
        logFilePath = logsDir.resolve("hw16-actuator.log");
        String logLine = "TEST-LOG-LINE-" + UUID.randomUUID();
        Files.writeString(logFilePath, logLine + System.lineSeparator(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        // Stub HealthIndicator dependency
        given(bookRepository.count()).willReturn(42L);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    @DisplayName("/actuator/health returns UP and custom library component with books.count")
    void healthEndpointShouldExposeCustomComponent() {
        @SuppressWarnings("unchecked")
        Map<String, Object> health = restTemplate.getForObject(url("/actuator/health"), Map.class);
        assertThat(health).isNotNull();
        assertThat(health.get("status")).isEqualTo("UP");

        @SuppressWarnings("unchecked")
        Map<String, Object> components = (Map<String, Object>) health.get("components");
        assertThat(components).isNotNull();

        @SuppressWarnings("unchecked")
        Map<String, Object> library = (Map<String, Object>) components.get("library");
        assertThat(library).isNotNull();
        assertThat(library.get("status")).isEqualTo("UP");

        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) library.get("details");
        assertThat(details).isNotNull();
        assertThat(((Number) details.get("books.count")).longValue()).isEqualTo(42L);
        assertThat(details.get("component")).isEqualTo("library");
    }

    @Test
    @DisplayName("/actuator/metrics returns 200 and contains names array")
    void metricsEndpointShouldReturnNames() {
        @SuppressWarnings("unchecked")
        Map<String, Object> metrics = restTemplate.getForObject(url("/actuator/metrics"), Map.class);
        assertThat(metrics).isNotNull();
        assertThat(metrics).containsKey("names");
        assertThat(metrics.get("names")).isInstanceOf(Iterable.class);
    }

    @Test
    @DisplayName("/actuator/logfile returns contents of configured log file")
    void logfileEndpointShouldReturnFileContents() {
        String body = restTemplate.getForObject(url("/actuator/logfile"), String.class);
        assertThat(body).isNotNull();
        assertThat(body).contains("TEST-LOG-LINE");
    }
}
