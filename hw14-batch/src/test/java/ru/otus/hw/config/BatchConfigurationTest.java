package ru.otus.hw.config;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;

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
    void shouldHaveCorrectJobConfiguration() {
        System.out.println("[DEBUG_LOG] Testing job configuration");
        assertThat(migrationJob.isRestartable()).isTrue();
        assertThat(migrationJob.getJobParametersValidator()).isNotNull();
    }
}