package ru.otus.hw.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;
import ru.otus.hw.models.jpa.Author;
import ru.otus.hw.models.jpa.Book;
import ru.otus.hw.models.jpa.Comment;
import ru.otus.hw.models.jpa.Genre;
import ru.otus.hw.models.mongo.MongoAuthor;
import ru.otus.hw.models.mongo.MongoBook;
import ru.otus.hw.models.mongo.MongoComment;
import ru.otus.hw.models.mongo.MongoGenre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.services.IdMappingService;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.batch.core.BatchStatus.COMPLETED;

@SpringBootTest
@SpringBatchTest
@TestPropertySource(properties = {
    "spring.data.mongodb.host=localhost",
    "spring.data.mongodb.port=27017",
    "spring.data.mongodb.database=test_library_integration",
    "spring.datasource.url=jdbc:h2:mem:integrationtestdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class MigrationJobIntegrationTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job migrationJob;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private IdMappingService idMappingService;

    @BeforeEach
    void setUp() {
        System.out.println("[DEBUG_LOG] Setting up integration test");
        
        // Clear all data
        idMappingService.clearMappings();
        
        // Clear MongoDB collections
        mongoTemplate.dropCollection(MongoBook.class);
        mongoTemplate.dropCollection(MongoAuthor.class);
        mongoTemplate.dropCollection(MongoGenre.class);
        mongoTemplate.dropCollection(MongoComment.class);
        
        // Clear JPA repositories
        commentRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();
        
        // Setup test data in MongoDB
        setupMongoTestData();
    }

    private void setupMongoTestData() {
        System.out.println("[DEBUG_LOG] Setting up MongoDB test data");
        
        // Create authors
        MongoAuthor author1 = new MongoAuthor("mongo-author-1", "Leo Tolstoy");
        MongoAuthor author2 = new MongoAuthor("mongo-author-2", "Fyodor Dostoevsky");
        mongoTemplate.save(author1);
        mongoTemplate.save(author2);
        
        // Create genres
        MongoGenre genre1 = new MongoGenre("mongo-genre-1", "Classic Literature");
        MongoGenre genre2 = new MongoGenre("mongo-genre-2", "Philosophy");
        MongoGenre genre3 = new MongoGenre("mongo-genre-3", "Drama");
        mongoTemplate.save(genre1);
        mongoTemplate.save(genre2);
        mongoTemplate.save(genre3);
        
        // Create books
        MongoBook book1 = new MongoBook("mongo-book-1", "War and Peace", author1, Arrays.asList(genre1, genre3));
        MongoBook book2 = new MongoBook("mongo-book-2", "Crime and Punishment", author2, Arrays.asList(genre1, genre2));
        mongoTemplate.save(book1);
        mongoTemplate.save(book2);
        
        // Create comments
        MongoComment comment1 = new MongoComment("mongo-comment-1", "Excellent masterpiece", book1);
        MongoComment comment2 = new MongoComment("mongo-comment-2", "Deep psychological analysis", book2);
        MongoComment comment3 = new MongoComment("mongo-comment-3", "Another great work", book1);
        mongoTemplate.save(comment1);
        mongoTemplate.save(comment2);
        mongoTemplate.save(comment3);
    }

    @Test
    void shouldCompleteFullMigrationJob() throws Exception {
        System.out.println("[DEBUG_LOG] Testing complete migration job");
        
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertThat(jobExecution.getStatus()).isEqualTo(COMPLETED);
        
        // Verify authors were migrated
        List<Author> authors = authorRepository.findAll();
        assertThat(authors).hasSize(2);
        assertThat(authors).extracting(Author::getFullName)
                .containsExactlyInAnyOrder("Leo Tolstoy", "Fyodor Dostoevsky");
        
        // Verify genres were migrated
        List<Genre> genres = genreRepository.findAll();
        assertThat(genres).hasSize(3);
        assertThat(genres).extracting(Genre::getName)
                .containsExactlyInAnyOrder("Classic Literature", "Philosophy", "Drama");
        
        // Verify books were migrated
        List<Book> books = bookRepository.findAll();
        assertThat(books).hasSize(2);
        assertThat(books).extracting(Book::getTitle)
                .containsExactlyInAnyOrder("War and Peace", "Crime and Punishment");
        
        // Verify comments were migrated
        List<Comment> comments = commentRepository.findAll();
        assertThat(comments).hasSize(3);
        assertThat(comments).extracting(Comment::getText)
                .containsExactlyInAnyOrder("Excellent masterpiece", "Deep psychological analysis", "Another great work");
        
        // Verify relationships are preserved
        Book warAndPeace = books.stream()
                .filter(book -> "War and Peace".equals(book.getTitle()))
                .findFirst()
                .orElseThrow();
        
        assertThat(warAndPeace.getAuthor().getFullName()).isEqualTo("Leo Tolstoy");
        assertThat(warAndPeace.getGenres()).hasSize(2);
        assertThat(warAndPeace.getGenres()).extracting(Genre::getName)
                .containsExactlyInAnyOrder("Classic Literature", "Drama");
        
        // Verify comment relationships
        long warAndPeaceComments = comments.stream()
                .filter(comment -> comment.getBook().getId() == warAndPeace.getId())
                .count();
        assertThat(warAndPeaceComments).isEqualTo(2);
    }

    @Test
    void shouldExecuteBookMigrationStepOnly() throws Exception {
        System.out.println("[DEBUG_LOG] Testing book migration step only");
        
        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("bookMigrationStep");

        // Then
        assertThat(jobExecution.getStatus()).isEqualTo(COMPLETED);
        
        // Verify only books, authors, and genres were migrated
        assertThat(authorRepository.findAll()).hasSize(2);
        assertThat(genreRepository.findAll()).hasSize(3);
        assertThat(bookRepository.findAll()).hasSize(2);
        assertThat(commentRepository.findAll()).hasSize(0); // Comments should not be migrated yet
    }

    @Test
    void shouldExecuteCommentMigrationStepOnly() throws Exception {
        System.out.println("[DEBUG_LOG] Testing comment migration step only");
        
        // Given - First run book migration to have books available for comments
        jobLauncherTestUtils.launchStep("bookMigrationStep");
        
        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("commentMigrationStep");

        // Then
        assertThat(jobExecution.getStatus()).isEqualTo(COMPLETED);
        
        // Verify comments were migrated
        List<Comment> comments = commentRepository.findAll();
        assertThat(comments).hasSize(3);
        
        // Verify comment-book relationships
        for (Comment comment : comments) {
            assertThat(comment.getBook()).isNotNull();
            assertThat(comment.getBook().getId()).isGreaterThan(0);
        }
    }

    @Test
    void shouldHandleEmptyMongoDatabase() throws Exception {
        System.out.println("[DEBUG_LOG] Testing migration with empty MongoDB");
        
        // Given - Clear all MongoDB data
        mongoTemplate.dropCollection(MongoBook.class);
        mongoTemplate.dropCollection(MongoAuthor.class);
        mongoTemplate.dropCollection(MongoGenre.class);
        mongoTemplate.dropCollection(MongoComment.class);
        
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertThat(jobExecution.getStatus()).isEqualTo(COMPLETED);
        
        // Verify no data was migrated
        assertThat(authorRepository.findAll()).hasSize(0);
        assertThat(genreRepository.findAll()).hasSize(0);
        assertThat(bookRepository.findAll()).hasSize(0);
        assertThat(commentRepository.findAll()).hasSize(0);
    }

    @Test
    void shouldMaintainIdMappingConsistency() throws Exception {
        System.out.println("[DEBUG_LOG] Testing ID mapping consistency");
        
        // Given
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertThat(jobExecution.getStatus()).isEqualTo(COMPLETED);
        
        // Verify ID mappings are consistent
        assertThat(idMappingService.getBookId("mongo-book-1")).isNotNull();
        assertThat(idMappingService.getBookId("mongo-book-2")).isNotNull();
        
        // Verify that comments reference correct book IDs
        List<Comment> comments = commentRepository.findAll();
        List<Book> books = bookRepository.findAll();
        
        for (Comment comment : comments) {
            boolean bookExists = books.stream()
                    .anyMatch(book -> book.getId() == comment.getBook().getId());
            assertThat(bookExists).isTrue();
        }
    }
}