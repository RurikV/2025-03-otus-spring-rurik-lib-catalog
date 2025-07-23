package ru.otus.hw.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import ru.otus.hw.batch.processors.BookItemProcessor;
import ru.otus.hw.batch.readers.MongoBookItemReader;
import ru.otus.hw.batch.writers.BookItemWriter;
import ru.otus.hw.models.jpa.Book;
import ru.otus.hw.models.mongo.MongoBook;

@Configuration
public class BatchConfiguration {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final MongoBookItemReader mongoBookItemReader;
    private final BookItemProcessor bookItemProcessor;
    private final BookItemWriter bookItemWriter;
    
    @Autowired
    public BatchConfiguration(JobRepository jobRepository,
                             PlatformTransactionManager transactionManager,
                             MongoBookItemReader mongoBookItemReader,
                             BookItemProcessor bookItemProcessor,
                             BookItemWriter bookItemWriter) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.mongoBookItemReader = mongoBookItemReader;
        this.bookItemProcessor = bookItemProcessor;
        this.bookItemWriter = bookItemWriter;
    }
    
    @Bean
    public Step migrationStep() {
        return new StepBuilder("migrationStep", jobRepository)
                .<MongoBook, Book>chunk(10, transactionManager)
                .reader(mongoBookItemReader)
                .processor(bookItemProcessor)
                .writer(bookItemWriter)
                .build();
    }
    
    @Bean
    public Job migrationJob() {
        return new JobBuilder("migrationJob", jobRepository)
                .start(migrationStep())
                .build();
    }
}