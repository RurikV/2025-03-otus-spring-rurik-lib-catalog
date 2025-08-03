package ru.otus.hw.config;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.models.mongo.MongoAuthor;
import ru.otus.hw.models.mongo.MongoBook;
import ru.otus.hw.models.mongo.MongoComment;
import ru.otus.hw.models.mongo.MongoGenre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ru.otus.hw.BatchMigrationApplication.class)
@SpringBatchTest
@TestPropertySource(properties = {
    "spring.data.mongodb.database=test_library",
    "spring.data.mongodb.port=27020",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.autoconfigure.exclude=org.springframework.shell.boot.StandardCommandsAutoConfiguration"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BatchConfigurationTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private BatchConfiguration batchConfiguration;

    @Autowired
    private Job migrationJob;

    @Autowired
    private Step bookMigrationStep;

    @Autowired
    private Step commentMigrationStep;

    @Autowired
    private Step genreMigrationStep;

    @Autowired
    private Step authorMigrationStep;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    void shouldCreateJobRepository() {
        System.out.println("[DEBUG_LOG] Testing JobRepository bean creation");
        assertThat(jobRepository).isNotNull();
    }

    @Test
    void shouldCreateTransactionManager() {
        System.out.println("[DEBUG_LOG] Testing PlatformTransactionManager bean creation");
        assertThat(transactionManager).isNotNull();
    }

    @Test
    void shouldCreateBatchConfiguration() {
        System.out.println("[DEBUG_LOG] Testing BatchConfiguration bean creation");
        assertThat(batchConfiguration).isNotNull();
    }

    @Test
    void shouldCreateMigrationJob() {
        System.out.println("[DEBUG_LOG] Testing migration job bean creation");
        assertThat(migrationJob).isNotNull();
        assertThat(migrationJob.getName()).isEqualTo("migrationJob");
    }

    @Test
    void shouldCreateBookMigrationStep() {
        System.out.println("[DEBUG_LOG] Testing book migration step bean creation");
        assertThat(bookMigrationStep).isNotNull();
        assertThat(bookMigrationStep.getName()).isEqualTo("bookMigrationStep");
    }

    @Test
    void shouldCreateCommentMigrationStep() {
        System.out.println("[DEBUG_LOG] Testing comment migration step bean creation");
        assertThat(commentMigrationStep).isNotNull();
        assertThat(commentMigrationStep.getName()).isEqualTo("commentMigrationStep");
    }

    @Test
    void shouldCreateGenreMigrationStep() {
        System.out.println("[DEBUG_LOG] Testing genre migration step bean creation");
        assertThat(genreMigrationStep).isNotNull();
        assertThat(genreMigrationStep.getName()).isEqualTo("genreMigrationStep");
    }

    @Test
    void shouldCreateAuthorMigrationStep() {
        System.out.println("[DEBUG_LOG] Testing author migration step bean creation");
        assertThat(authorMigrationStep).isNotNull();
        assertThat(authorMigrationStep.getName()).isEqualTo("authorMigrationStep");
    }

    @Test
    void shouldHaveCorrectJobConfiguration() {
        System.out.println("[DEBUG_LOG] Testing job configuration");
        assertThat(migrationJob.isRestartable()).isTrue();
        assertThat(migrationJob.getJobParametersValidator()).isNotNull();
    }

    @Test
    void shouldMigrateAllDataCorrectly() throws Exception {
        System.out.println("[DEBUG_LOG] Testing complete migration verification");
        
        // Clear MongoDB collections before migration
        mongoTemplate.dropCollection(MongoGenre.class);
        mongoTemplate.dropCollection(MongoAuthor.class);
        mongoTemplate.dropCollection(MongoBook.class);
        mongoTemplate.dropCollection(MongoComment.class);
        
        // Get source data counts
        long sourceGenreCount = genreRepository.count();
        long sourceAuthorCount = authorRepository.count();
        long sourceBookCount = bookRepository.count();
        long sourceCommentCount = commentRepository.count();
        
        System.out.println("[DEBUG_LOG] Source data - Genres: " + sourceGenreCount + 
                          ", Authors: " + sourceAuthorCount + 
                          ", Books: " + sourceBookCount + 
                          ", Comments: " + sourceCommentCount);
        
        // Execute migration job
        JobExecution jobExecution = jobLauncher.run(migrationJob, new JobParameters());
        
        // Verify job completed successfully
        assertThat(jobExecution.getStatus().isUnsuccessful()).isFalse();
        System.out.println("[DEBUG_LOG] Migration job status: " + jobExecution.getStatus());
        
        // Verify all data migrated correctly
        long migratedGenreCount = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), MongoGenre.class);
        long migratedAuthorCount = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), MongoAuthor.class);
        long migratedBookCount = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), MongoBook.class);
        long migratedCommentCount = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), MongoComment.class);
        
        System.out.println("[DEBUG_LOG] Migrated data - Genres: " + migratedGenreCount + 
                          ", Authors: " + migratedAuthorCount + 
                          ", Books: " + migratedBookCount + 
                          ", Comments: " + migratedCommentCount);
        
        // Assert all records migrated
        assertThat(migratedGenreCount).isEqualTo(sourceGenreCount);
        assertThat(migratedAuthorCount).isEqualTo(sourceAuthorCount);
        assertThat(migratedBookCount).isEqualTo(sourceBookCount);
        assertThat(migratedCommentCount).isEqualTo(sourceCommentCount);
        
        System.out.println("[DEBUG_LOG] Migration verification completed successfully - all records migrated");
    }
}